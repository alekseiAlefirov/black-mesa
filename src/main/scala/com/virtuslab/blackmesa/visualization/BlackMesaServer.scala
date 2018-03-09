package com.virtuslab.blackmesa.visualization

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object BlackMesaServer extends App {

  implicit val system: ActorSystem = ActorSystem("blackMesaServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = ???

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
