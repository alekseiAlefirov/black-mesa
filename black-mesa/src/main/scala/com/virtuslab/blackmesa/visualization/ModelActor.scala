package com.virtuslab.blackmesa.visualization

import akka.actor.{ Actor, ActorRef }
import com.virtuslab.blackmesa.model.Model
import com.virtuslab.blackmesa.visualization.ModelActor._

class ModelActor(val configuration: Configuration) extends Actor with JsonSupport {

  import spray.json._

  private val Configuration(modelFactory, visualizationElements, _, modelParams, _, _) = configuration

  private var currentModelParams: ModelParameters = modelParams
  private var model: Model = modelFactory(currentModelParams)

  println("ModelActor started!")

  import com.virtuslab.blackmesa.visualization.Protocol._

  override def receive: Receive = {
    case IncomingConnection(out) => context.become(connected(out))
  }

  def renderModel: List[JsValue] = visualizationElements.map(_.render(model)).toList

  def connected(out: ActorRef): Receive = {
    case Reset =>
      model = modelFactory(currentModelParams)
      out ! VizState(renderModel)

    case GetStep(_) => // LBIALY apparently the step index is never used in mesa? ModularVisualization.py:195
      if (!model.running)
        out ! ModelEnd
      else {
        model.step()
        out ! VizState(renderModel)
      }

    case SubmitParams(param, value) =>
      currentModelParams = currentModelParams.find {
        case (paramName, currentParamValue) =>
          paramName == param && currentParamValue.isInstanceOf[UserSettableParameter]
      } match {
        case Some((_, userSettableParam: UserSettableParameter)) =>
          currentModelParams + (param -> userSettableParam.withNewValue(value))
        case Some((_, _: AnyParameter)) =>
          currentModelParams + (param -> AnyParameter(value))
        case None =>
          currentModelParams
      }

    case GetParams =>
      val paramsMap = getUserParams.map(p => (p.name, p)).toMap.mapValues(_.toJson)
      out ! ModelParams(JsObject(paramsMap))

    case unknown => println(s"Unknown message received: $unknown")
  }

  def getUserParams: List[UserSettableParameter] = {
    currentModelParams.filter(_._2.isInstanceOf[UserSettableParameter])
      .map(_._2.asInstanceOf[UserSettableParameter])
      .toList
  }

  override def postStop(): Unit = {
    println("Model actor shutdown finished!")
  }

}

object ModelActor {

  case class IncomingConnection(outbound: ActorRef)

  sealed trait ModelData

  case object NoData extends ModelData

  case class Data(out: ActorRef) extends ModelData

  sealed trait ModelState

  case object NotConnected extends ModelState

  case object Connected extends ModelState

}
