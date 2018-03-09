package com.virtuslab.blackmesa.model

import scala.util.Random

case class Model(seed: Long = System.currentTimeMillis()) {
  val random: Random = new Random(seed)
  var running = true
  var schedule = None

  def runModel(): Unit = {
    while (running) {
      step()
    }
  }

  def step(): Unit = {}

}
