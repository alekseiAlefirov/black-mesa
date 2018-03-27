package org.virtuslab.blackmesa.visualization.modules

import org.virtuslab.blackmesa.model.Agent
import org.virtuslab.blackmesa.visualization.JsonSupport
import org.virtuslab.blackmesa.visualization.VisualizationElement
import org.virtuslab.blackmesa.model.Model
import spray.json.JsValue
import spray.json._

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
