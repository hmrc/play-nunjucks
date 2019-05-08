package nunjucks

import akka.actor.Actor
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.{Environment, PlayException}

import scala.concurrent.ExecutionContext
import scala.util.Failure

class NunjucksActor (
                      environment: Environment,
                      context: NunjucksContext,
                      private implicit val ec: ExecutionContext
                    ) extends Actor {

  import NunjucksActor._

  private val nunjucks = Nunjucks(context, environment.classLoader)

  override def receive: Receive = {
    case Render(view, params, messages, request) =>
      sender ! nunjucks.render(view, params, messages, request)
    case Stop =>
      nunjucks.release()
  }

  override def finalize(): Unit = {
    super.finalize()
    nunjucks.release()
  }
}

object NunjucksActor {
  case class Render(view: String, params: JsObject, messages: Messages, request: RequestHeader)
  case object Stop
}
