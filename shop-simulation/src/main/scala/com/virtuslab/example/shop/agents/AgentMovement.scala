package com.virtuslab.example.shop.agents

import com.virtuslab.blackmesa.model.Model

import scala.util.Random

trait AgentMovement {
  def move(pos: (Int, Int), model: Model): (Int, Int)
}

case object RandomMovement extends AgentMovement {
  override def move(pos: (Int, Int), model: Model): (Int, Int) = findPossibleMovements(pos, model).random()

  private def findPossibleMovements(pos: (Int, Int), model: Model) = {
    for {
      x <- findCoordinate(pos._1, model.grid.width)
      y <- findCoordinate(pos._2, model.grid.height)
      if (model.grid.getCellListContents((x, y)).toSet - this).isEmpty
    } yield (x, y)
  }

  private def findCoordinate(oldCoordinate: Int, upperRange: Int): Seq[Int] = {
    (-1 to 1).map(oldCoordinate - _).filter(_ inRange (0, upperRange))
  }

  implicit class InRange(x: Int) {
    def inRange(range: (Int, Int)): Boolean = x >= range._1 && x < range._2
  }

  implicit class RandomMovement(movements: Seq[(Int, Int)]) {
    val rand = new Random()
    def random(): (Int, Int) = movements(rand.nextInt(movements.length))
  }
}
