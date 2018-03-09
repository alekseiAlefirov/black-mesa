package com.virtuslab.blackmesa.model

case class Agent(uniqueId: Int, model: Model) {

  def callStage(stage: String): Unit = {
    stage match {
      case "step" => step()
    }
  }

  def advance(): Unit = {}

  def step(): Unit = {}

  var pos: (Int, Int) = null

}
