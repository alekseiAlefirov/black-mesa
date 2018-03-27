package com.virtuslab.example.shop

import com.virtuslab.blackmesa.model.Agent
import com.virtuslab.blackmesa.model.space.Grid
import com.virtuslab.blackmesa.visualization.modules.CanvasGridVisualization
import com.virtuslab.blackmesa.visualization.modules.Circle
import com.virtuslab.blackmesa.visualization.modules.Portrayal
import com.virtuslab.blackmesa.visualization.modules.Rect
import com.virtuslab.blackmesa.visualization.BlackMesaServer
import com.virtuslab.blackmesa.visualization.Configuration
import com.virtuslab.example.shop.agents.Agents.Shelf
import com.virtuslab.example.shop.agents.Agents.Shopper

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
