package org.virtuslab.blackmesa.model

import org.virtuslab.blackmesa.model.space.Grid
import spray.json.JsObject

import scala.util.Random

abstract class Model(seed: Long = System.currentTimeMillis()) {
  val random: Random = new Random(seed)

  def running = true

  def schedule: BaseScheduler // LBIALY todo maybe generic subtype?

  def grid: Grid[Agent]

  def modelVars: JsObject = JsObject.empty

  def description: String = ""

  def runModel(): Unit = {
    while (running) {
      step()
    }
  }

  def step(): Unit = {}

}
