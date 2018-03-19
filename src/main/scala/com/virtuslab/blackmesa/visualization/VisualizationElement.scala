package com.virtuslab.blackmesa.visualization

import com.virtuslab.blackmesa.model.Model
import spray.json.JsValue

trait VisualizationElement {

  def packageIncludes: Seq[String]

  def localIncludes: Seq[String]

  def jsCode = "" // this seems to be a horrible idea

  def render(model: Model): JsValue

}
