package com.virtuslab.blackmesa.example.shop

import com.virtuslab.blackmesa.example.shop.agents.Agents.{ Shelf, Shopper }
import com.virtuslab.blackmesa.model.Agent
import com.virtuslab.blackmesa.model.space.Grid
import com.virtuslab.blackmesa.visualization.modules.{ CanvasGridVisualization, Circle, Portrayal, Rect }
import com.virtuslab.blackmesa.visualization.{ BlackMesaServer, Configuration }

object Simulation extends App {

  val width = 50
  val height = 25

  val portrayalFunction: Agent => Portrayal = {
    case Shelf(_, (x, y), false) => Rect(x, y, 1, 1, "#888888", 0)
    case Shelf(_, (x, y), true) => Rect(x, y, 1, 1, "#ff1a1a", 1)
    case Shopper(_, (x, y)) => Circle(x, y, 1, "#3399ff", 3)
  }

  val canvasVisualization = new CanvasGridVisualization(
    portrayalFunction,
    gridWidth = width,
    gridHeight = height,
    canvasWidth = 1000,
    canvasHeight = 500)

  val grid: Grid[Agent] = new Grid[Agent](width, height, true)

  import InitialState._
  val model: ShopModel = ShopModel(grid).withAgents(initialState)

  val configuration = Configuration(_ => model, IndexedSeq(canvasVisualization), "Shop simulation")

  new BlackMesaServer(configuration).start()

}
