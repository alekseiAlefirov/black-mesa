package com.virtuslab.example.shop.agents

import java.util.concurrent.atomic.AtomicInteger

import com.virtuslab.blackmesa.model.{ Agent, Model }

object Agents {

  val atomicInt: AtomicInteger = new AtomicInteger()

  val movement: AgentMovement = RandomMovement

  case class Shopper(uniqueId: Int = atomicInt.getAndIncrement(), override var pos: (Int, Int)) extends Agent {

    override def step(model: Model): Unit = {
      collectProduct(model)
      model.grid.moveAgent(this, movement.move(pos, model))
    }

    private def collectProduct(model: Model): Unit = {
      Some(this.pos)
        .map { case (x, y) => (x - 1, y) }
        .filter { case (x, _) => x >= 0 }
        .flatMap(findShelf(model))
        .map(_.takeProduct)
        .foreach(shelf => model.grid.moveAgent(shelf, shelf.pos))
    }

    private def findShelf(model: Model)(position: (Int, Int)): Option[Shelf] = {
      model.grid.getCellListContents(position)
        .headOption
        .flatMap(agent => agent match {
          case shelf: Shelf => Some(shelf)
          case _ => None
        })
    }
  }

  case class Shelf(uniqueId: Int = atomicInt.getAndIncrement(), override var pos: (Int, Int), hasProduct: Boolean = false) extends Agent {
    def withProduct: Shelf = {
      this.copy(hasProduct = true)
    }

    def takeProduct: Shelf = {
      this.copy(hasProduct = false)
    }
  }

  case class Product(uniqueId: Int = atomicInt.getAndIncrement(), override var pos: (Int, Int)) extends Agent

}
