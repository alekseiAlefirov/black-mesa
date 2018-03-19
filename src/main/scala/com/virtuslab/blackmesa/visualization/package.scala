package com.virtuslab.blackmesa

import com.virtuslab.blackmesa.model.Model
import spray.json.{ JsObject, JsValue }

package object visualization {

  sealed trait Parameter

  case class AnyParameter(value: JsValue) extends Parameter

  sealed trait UserParameterType extends Product

  case object Number extends UserParameterType {
    override def productPrefix: String = "number"
  }

  case object Checkbox extends UserParameterType {
    override def productPrefix: String = "checkbox"
  }

  case class Choice(choices: List[String]) extends UserParameterType {
    require(choices.nonEmpty)

    override def productPrefix: String = "checkbox"
  }

  case class Slider(minValue: Double, maxValue: Double) extends UserParameterType {
    override def productPrefix: String = "slider"
  }

  case object StaticText extends UserParameterType {
    override def productPrefix: String = "static_text"
  }

  case class UserSettableParameter(
    paramType: UserParameterType,
    name: String,
    description: String,
    step: Int = 1,
    value: Any) extends Parameter {

    import spray.json.DefaultJsonProtocol._

    // NOT MY FINEST WORK, WE ARE JUST TRYING TO BE COMPLIANT WITH MESA'S FRONTEND ;_;
    def withNewValue(value: JsValue): UserSettableParameter = {
      paramType match {
        case Number => UserSettableParameter(paramType, name, description, step, value.convertTo[Double])
        case Checkbox => UserSettableParameter(paramType, name, description, step, value.convertTo[Boolean])
        case Choice(choices) =>
          val newValue = value.convertTo[String]
          if (choices contains newValue) UserSettableParameter(paramType, name, description, step, newValue)
          else {
            println(s"this is stupid but mesa does this: you have passed a value that is not one of possible choices - $newValue (valid choices $choices)")
            this
          }
        case Slider(minValue, maxValue) =>
          val newValue = value.convertTo[Double]
          val boundedValue = if (newValue > maxValue) maxValue else if (newValue < minValue) minValue else newValue
          UserSettableParameter(paramType, name, description, step, boundedValue)
        case StaticText => UserSettableParameter(paramType, name, description, step, value.convertTo[String])
      }
    }

  }

  object Protocol {

    // Server -> Client

    sealed trait ServerMessage extends Product

    case class VizState(data: List[JsValue]) extends ServerMessage {
      override def productPrefix: String = "viz_state"
    }

    case class ModelParams(params: JsObject) extends ServerMessage {
      override def productPrefix: String = "model_params"
    }

    case object ModelEnd extends ServerMessage {
      override def productPrefix: String = "end"
    }

    // Client -> Server
    sealed trait ClientMessage extends Product

    case class InvalidMessage(text: String) extends ClientMessage

    case class GetStep(step: Long) extends ClientMessage {
      override def productPrefix: String = "get_step"
    }

    case class SubmitParams(param: String, value: JsValue) extends ClientMessage {
      override def productPrefix: String = "submit_params"
    }

    case object Reset extends ClientMessage {
      override def productPrefix: String = "reset"
    }

    case object GetParams extends ClientMessage {
      override def productPrefix: String = "get_params"
    }

  }

  type ModelParameters = Map[String, Parameter]

  case class Configuration(
    modelFactory: ModelParameters => Model,
    visualizationElements: IndexedSeq[VisualizationElement],
    modelName: String,
    modelParams: Map[String, Parameter],
    description: String = "No description available",
    port: Int = 8080)

}
