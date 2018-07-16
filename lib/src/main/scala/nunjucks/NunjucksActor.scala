package nunjucks

import akka.actor.Actor
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.JsValue

import scala.util.Try

class NunjucksActor (
                      environment: Environment,
                      context: NunjucksContext
                    ) extends Actor {

  import NunjucksActor._

  private val nunjucks = new Nunjucks(environment, context)

  override def receive: Receive = {

    case Render(view, params, messages) =>
      sender ! Try(nunjucks.render(view, params, messages))

    case Stop =>
      nunjucks.release()
  }

  override def finalize(): Unit = {
    super.finalize()
    nunjucks.release()
  }
}

object NunjucksActor {
  case class Render(view: String, params: JsValue, messages: Messages)
  case object Stop
}
