package com.virtuslab.example.shop.agents

import com.virtuslab.blackmesa.model.Model

import scala.util.Random

trait AgentMovement {
  def move(pos: (Int, Int), model: Model): (Int, Int)
}

case object RandomMovement extends AgentMovement {
  val random = new Random(System.currentTimeMillis())

  override def move(pos: (Int, Int), model: Model): (Int, Int) = randomMovement(findPossibleMovements(pos, model))

  private def findPossibleMovements(pos: (Int, Int), model: Model) = {
    for {
      x <- findCoordinate(pos._1, model.grid.width)
      y <- findCoordinate(pos._2, model.grid.height)
      if (model.grid.getCellListContents((x, y)).toSet - this).isEmpty
    } yield (x, y)
  }

  private def findCoordinate(oldCoordinate: Int, upperRange: Int): Seq[Int] = {
    (-1 to 1).map(oldCoordinate - _).filter(0 until upperRange contains)
  }

  private def randomMovement(movements: Seq[(Int, Int)]): (Int, Int) = movements(random.nextInt(movements.length))

}
