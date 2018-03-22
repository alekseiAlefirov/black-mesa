package com.virtuslab.blackmesa.visualization.modules

import com.virtuslab.blackmesa.model.{ Agent, Model }
import com.virtuslab.blackmesa.visualization.{ JsonSupport, VisualizationElement }
import spray.json.{ JsValue, _ }

class CanvasGridVisualization(
  portrayalFunction: Agent => Portrayal,
  gridWidth: Int, gridHeight: Int,
  canvasWidth: Int = 500,
  canvasHeight: Int = 500) extends VisualizationElement with JsonSupport {

  override def packageIncludes: Seq[String] = Seq("GridDraw.js", "CanvasModule.js")

  override def localIncludes: Seq[String] = Seq.empty

  override def render(model: Model): JsValue = model.grid
    .getAllAgents
    .map(portrayalFunction)
    .groupBy(_.layer)
    .toJson

  override def jsCode: String =
    s"""elements.push(
       |  new CanvasModule($canvasWidth, $canvasHeight, $gridWidth, $gridHeight)
       |)""".stripMargin
}
