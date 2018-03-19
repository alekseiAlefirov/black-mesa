package com.virtuslab.blackmesa.model

case class Agent(uniqueId: Int, model: Model) {

  var pos: (Int, Int) = null

  def callStage(stage: String): Unit = {
    stage match {
      case "step" => step()
    }
  }

  def step(): Unit = {}

  def advance(): Unit = {}

}
