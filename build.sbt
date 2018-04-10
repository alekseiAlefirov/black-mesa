scalaVersion := "2.12.5"

name := "black-mesa"

organization := "org.virtuslab"

version := "0.0.1"

lazy val mesa = (project in file("black-mesa"))
  .enablePlugins(SbtTwirl)

lazy val shop = (project in file("shop-simulation"))
  .dependsOn(mesa)


lazy val root = (project in file("."))
  .aggregate(mesa, shop)
