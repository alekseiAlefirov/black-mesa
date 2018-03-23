package com.virtuslab.blackmesa.visualization.modules

//TODO implement all portrayal types
sealed trait Portrayal {
  def shape: String

  def layer: Int
}

case class Circle(
  x: Int,
  y: Int,
  r: Double,
  color: String,
  layer: Int,
  filled: Boolean = true) extends Portrayal {
  val shape = "circle"
}

case class Rect(
  x: Int,
  y: Int,
  w: Double,
  h: Double,
  color: String,
  layer: Int,
  filled: Boolean = true) extends Portrayal {
  val shape = "rect"
}