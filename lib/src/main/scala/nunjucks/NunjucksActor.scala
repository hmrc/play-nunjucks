package nunjucks

import akka.actor.Actor
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.JsObject

import scala.concurrent.ExecutionContext

class NunjucksActor (
                      environment: Environment,
                      context: NunjucksContext
                    )(implicit ec: ExecutionContext) extends Actor {

  import NunjucksActor._

  private val nunjucks = Nunjucks(context)

  override def receive: Receive = {
    case Render(view, params, messages) =>
      sender ! nunjucks.render(view, params, messages)
    case Stop =>
      nunjucks.release()
  }

  override def finalize(): Unit = {
    super.finalize()
    nunjucks.release()
  }
}

object NunjucksActor {
  case class Render(view: String, params: JsObject, messages: Messages)
  case object Stop
}
