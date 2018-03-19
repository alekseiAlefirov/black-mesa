package com.virtuslab.blackmesa.visualization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.virtuslab.blackmesa.visualization.Protocol._
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._
  import spray.json._

  private implicit lazy val anyParameterFormat: RootJsonFormat[AnyParameter] = jsonFormat1(AnyParameter)

  // client -> server
  private implicit lazy val getStepFormat: RootJsonFormat[GetStep] = jsonFormat1(GetStep)
  private implicit lazy val submitParamsFormat: RootJsonFormat[SubmitParams] = jsonFormat2(SubmitParams)
  private implicit lazy val invalidMessageFormat: RootJsonFormat[InvalidMessage] = jsonFormat1(InvalidMessage)

  // server -> client
  private implicit lazy val vizStateFormat: RootJsonFormat[VizState] = jsonFormat1(VizState)
  private implicit lazy val modelParamsFormat: RootJsonFormat[ModelParams] = jsonFormat1(ModelParams)

  implicit lazy val clientMessageFormat: RootJsonFormat[ClientMessage] = new RootJsonFormat[ClientMessage] {
    def read(json: JsValue): ClientMessage = json.asJsObject.fields.get("type") match {
      case Some(JsString("reset")) => Reset
      case Some(JsString("get_params")) => GetParams
      case Some(JsString("get_step")) => json.convertTo[GetStep]
      case Some(JsString("submit_params")) => json.convertTo[SubmitParams]
      case Some(JsString(unknownType)) => throw new IllegalArgumentException(s"type '$unknownType' is invalid")
      case Some(_) | None => borkedType(json)
    }

    def write(obj: ClientMessage): JsValue = JsObject((obj match {
      case Reset => JsObject()
      case GetParams => JsObject()
      case getStep: GetStep => getStep.toJson
      case submitParams: SubmitParams => submitParams.toJson
      case invalidMessage: InvalidMessage => invalidMessage.toJson
    }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
  }

  implicit lazy val serverMessageFormat: RootJsonFormat[ServerMessage] = new RootJsonFormat[ServerMessage] {
    def read(json: JsValue): ServerMessage = json.asJsObject.fields.get("type") match {
      case Some(JsString("end")) => ModelEnd
      case Some(JsString("viz_state")) => json.convertTo[VizState]
      case Some(JsString("model_params")) => json.convertTo[ModelParams]
      case Some(JsString(unknownType)) => throw new IllegalArgumentException(s"type '$unknownType' is invalid")
      case Some(_) | None => borkedType(json)
    }

    def write(obj: ServerMessage): JsValue = JsObject((obj match {
      case ModelEnd => JsObject()
      case vizState: VizState => vizState.toJson
      case modelParams: ModelParams => modelParams.toJson
    }).asJsObject.fields + ("type" -> JsString(obj.productPrefix)))
  }

  implicit lazy val userSettableParameterFormat: RootJsonFormat[UserSettableParameter] =
    new RootJsonFormat[UserSettableParameter] {
      override def read(json: JsValue): UserSettableParameter = {
        val fields = json.asJsObject.fields
        fields("param_type") match {
          case JsString("number") =>
            UserSettableParameter(
              Number, fields("name").convertTo[String], fields("description").convertTo[String],
              fields("step").convertTo[Int], fields("value").convertTo[Double])
          case JsString("slider") =>
            UserSettableParameter(
              Slider(fields("min_value").convertTo[Double], fields("max_value").convertTo[Double]),
              fields("name").convertTo[String], fields("description").convertTo[String],
              fields("step").convertTo[Int], fields("value").convertTo[Double])
          case JsString("checkbox") =>
            UserSettableParameter(
              Checkbox, fields("name").convertTo[String], fields("description").convertTo[String],
              fields("step").convertTo[Int], fields("value").convertTo[Boolean])
          case JsString("choice") =>
            UserSettableParameter(
              Choice(fields("choices").convertTo[List[String]]),
              fields("name").convertTo[String], fields("description").convertTo[String],
              fields("step").convertTo[Int], fields("value").convertTo[String])
          case JsString("static_text") =>
            UserSettableParameter(
              StaticText, fields("name").convertTo[String], fields("description").convertTo[String],
              fields("step").convertTo[Int], fields("value").convertTo[String])
        }
      }

      override def write(obj: UserSettableParameter): JsValue = {
        obj.paramType match {
          case Number => JsObject(
            "name" -> JsString(obj.name),
            "description" -> JsString(obj.description),
            "step" -> JsNumber(obj.step),
            "param_type" -> JsString("number"),
            "value" -> JsNumber(obj.value.asInstanceOf[Double]))
          case Slider(min, max) => JsObject(
            "name" -> JsString(obj.name),
            "description" -> JsString(obj.description),
            "step" -> JsNumber(obj.step),
            "param_type" -> JsString("slider"),
            "min_value" -> JsNumber(min),
            "max_value" -> JsNumber(max),
            "value" -> JsNumber(obj.value.asInstanceOf[Double]))
          case Checkbox => JsObject(
            "name" -> JsString(obj.name),
            "description" -> JsString(obj.description),
            "param_type" -> JsString("checkbox"),
            "step" -> JsNumber(obj.step),
            "value" -> JsBoolean(obj.value.asInstanceOf[Boolean]))
          case Choice(choices) => JsObject(
            "name" -> JsString(obj.name),
            "description" -> JsString(obj.description),
            "param_type" -> JsString("choice"),
            "step" -> JsNumber(obj.step),
            "choices" -> JsArray(choices.toVector.map(JsString(_))),
            "value" -> JsString(obj.value.asInstanceOf[String]))
          case StaticText => JsObject(
            "name" -> JsString(obj.name),
            "description" -> JsString(obj.description),
            "param_type" -> JsString("static_text"),
            "step" -> JsNumber(obj.step),
            "value" -> JsString(obj.value.asInstanceOf[String]))
        }
      }
    }

  implicit lazy val parameterFormat: RootJsonFormat[Parameter] = new RootJsonFormat[Parameter] {
    def read(json: JsValue): Parameter = {
      ??? // should not be ever used!
    }

    def write(obj: Parameter): JsValue = obj match {
      case o: AnyParameter => ??? // should not be ever used!
      case o: UserSettableParameter => o.toJson
    }
  }

  private def borkedType(json: JsValue) = {
    throw new IllegalArgumentException(s"message ${json.compactPrint} doesn't have a type or it's value is invalid!")
  }
}
