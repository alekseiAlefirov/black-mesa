package com.virtuslab.blackmesa.model

import scala.util.Random

abstract class Model(seed: Long = System.currentTimeMillis()) {
  val random: Random = new Random(seed)
  def running = true
  def schedule: BaseScheduler // LBIALY todo maybe generic subtype?

  def runModel(): Unit = {
    while (running) {
      step()
    }
  }

  def step(): Unit = {}

  def grid: Grid[_]

}
