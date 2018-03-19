package com.virtuslab.blackmesa.visualization

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

class BlackMesaServer(protected val configuration: Configuration) extends Routing {

  implicit val system: ActorSystem = ActorSystem("blackMesaServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def start(): Unit = {
    Await.ready(Http().bindAndHandle(routes, "0.0.0.0", configuration.port), Duration.Inf)

    println(s"Server online at http://0.0.0.0:${configuration.port}/")
    println("Press enter to exit.")

    StdIn.readLine()

    system.terminate()
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
