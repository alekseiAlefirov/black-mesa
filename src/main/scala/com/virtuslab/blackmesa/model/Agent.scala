package com.virtuslab.blackmesa.model

import spray.json.JsObject

trait Agent {

  type Position = (Int, Int)

  var pos: Position

  def callStage(stage: String, model: Model): Unit = {
    stage match {
      case "step" => step(model)
    }
  }

  def step(model: Model): Unit = {}

  def advance(): Unit = {}

  def agentVars: JsObject = ???

}