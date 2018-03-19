package com.virtuslab.blackmesa.visualization.modules

import com.virtuslab.blackmesa.model.{ Agent, Model }
import com.virtuslab.blackmesa.visualization.VisualizationElement
import spray.json.JsValue

class CanvasGridVisualization(
  portrayalFunction: Agent => JsValue,
  gridWidth: Int, gridHeight: Int,
  canvasWidth: Int = 500,
  canvasHeight: Int = 500) extends VisualizationElement {
  override def packageIncludes: Seq[String] = Seq("GridDraw.js", "CanvasModule.js")

  override def localIncludes: Seq[String] = Seq.empty

  override def render(model: Model): JsValue = ???

  //    model match {
  //    case m: Model with GridAware[A,] =>
  //    }

  override def jsCode: String =
    s"""elements.push(
       |  new CanvasModule($canvasWidth, $canvasHeight, $gridWidth, $gridHeight)"
       |)""".stripMargin
}
