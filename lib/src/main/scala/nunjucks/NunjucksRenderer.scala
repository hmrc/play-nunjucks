package nunjucks

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.{Json, Writes}
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal


@Singleton
class NunjucksRenderer @Inject() (
                                   system: ActorSystem,
                                   environment: Environment,
                                   context: NunjucksContext
                                 )(implicit ec: ExecutionContext) {

  private implicit lazy val timeout: Timeout = 2.seconds

  private val restartStrategy: SupervisorStrategy = OneForOneStrategy() {
    case NonFatal(_) => Escalate
    case _           => Escalate
  }

  private val actor = {
    val actor = FromConfig(supervisorStrategy = restartStrategy)
      .props(Props(new NunjucksActor(environment, context)))
    system.actorOf(actor, "nunjucks-actor")
  }

  def render[A : Writes](view: String, params: A)(implicit messages: Messages): Future[Html] = {
    (actor ? NunjucksActor.Render(view, Json.toJson(params), messages)).mapTo[Try[String]].map {
      result =>
        Html(result.get)
    }
  }
}
