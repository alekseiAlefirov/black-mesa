package com.virtuslab.blackmesa.visualization

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`text/html`
import akka.http.scaladsl.server.{ Directives, Route }
import play.twirl.api.Html

trait Routing extends ActorBasedModelExecution with JsonSupport {

  import Directives._

  protected val routes: Route =
    pathPrefix("static") {
      getFromResourceDirectory("static")
    } ~
      pathPrefix("local") {
        getFromDirectory("local")
      } ~
      path("ws") {
        get {
          handleWebSocketMessages(startNewModelActorFlow)
        }
      } ~
      pathEndOrSingleSlash {
        get {
          val (packageIncludes, localIncludes, jsSnippets) = includesFromVisualisationElements
          complete(html.index(
            configuration.modelName,
            configuration.description,
            packageIncludes,
            localIncludes,
            configuration.port,
            jsSnippets))
        }
      }

  protected def configuration: Configuration

  implicit private val twirlHtmlMarshaller: ToEntityMarshaller[Html] =
    Marshaller.StringMarshaller.wrap(`text/html`)(_.toString)

  private def includesFromVisualisationElements: (Set[String], Set[String], List[String]) = {
    configuration.visualizationElements.foldLeft((Set.empty[String], Set.empty[String], List.empty[String])) {
      case ((packageIncludes, localIncludes, jsSnippets), elem) =>
        (packageIncludes ++ elem.packageIncludes, localIncludes ++ elem.localIncludes, jsSnippets :+ elem.jsCode)
    }
  }
}
