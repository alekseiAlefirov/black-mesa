package org.virtuslab.example.shop

import org.virtuslab.blackmesa.model.Agent
import org.virtuslab.example.shop.agents.Agents.Shelf
import org.virtuslab.example.shop.agents.Agents.Shopper

import scala.util.Random

object InitialState {
  val ShelvesDistance = 5
  val NumberOfShelves = 9
  val ShelfSize = 15

  val NumberOfShoppers = 15

  val random = new Random

  def initialState: List[Agent] = {
    val shelves: List[Shelf] = getShelves
    shoppers(shelves) ++ withProducts(shelves)
  }

  private def getShelves: List[Shelf] = for {
    x <- Stream.iterate(ShelvesDistance)(_ + ShelvesDistance).take(NumberOfShelves).toList
    y <- 1 to ShelfSize
  } yield Shelf(pos = (x, y))

  private def shoppers(shelves: List[Shelf]): List[Shopper] = Stream.continually((shelves.map(_.pos), randomPosition))
    .filter { case (takenPos, randomPos) => !takenPos.contains(randomPos) }
    .map { case (takenPos, randomPos) => (randomPos :: takenPos, randomPos) }
    .take(NumberOfShoppers)
    .map { case (_, pos) => pos }
    .map(position => Shopper(pos = position))
    .toList

  private def randomPosition: (Int, Int) = (random.nextInt(Simulation.width), random.nextInt(Simulation.height))

  private def withProducts(shelves: List[Shelf]): List[Shelf] = shelves.map(s => if (random.nextBoolean) s.withProduct else s)

}
