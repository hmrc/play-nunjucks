package nunjucks

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, Writes}
import play.twirl.api.Html

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal


@Singleton
class NunjucksRenderer @Inject() (
                                   system: ActorSystem,
                                   environment: Environment,
                                   njkContext: NunjucksContext
                                 )(implicit ec: ExecutionContext) {

  private implicit lazy val timeout: Timeout = njkContext.timeout

  private val restartStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: ExceptionInInitializerError => Escalate
    case NonFatal(_)                    => Restart
    case _                              => Escalate
  }

  private val actor = {
    val actor = FromConfig(supervisorStrategy = restartStrategy)
      .props(Props(new NunjucksActor(environment, njkContext)))
    system.actorOf(actor, "nunjucks-actor")
  }

  def renderAsync[A : Writes](view: String, context: A)(implicit messages: Messages): Future[Html] = {
    Future.fromTry {
      Try { Json.toJson(context).asInstanceOf[JsObject] }
    }.flatMap {
      ctx =>
        (actor ? NunjucksActor.Render(view, ctx, messages))
          .mapTo[Try[String]]
          .flatMap {
            result =>
              Future.fromTry(result).map(Html(_))
          }
    }
  }

  def render[A : Writes](
                          view: String,
                          context: A,
                          timeout: FiniteDuration = njkContext.timeout
                        )(implicit messages: Messages): Html = {

    Await.result(renderAsync(view, context), timeout)
  }
}
