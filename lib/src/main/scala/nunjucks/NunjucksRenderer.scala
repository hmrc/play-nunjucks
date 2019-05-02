package nunjucks

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.i18n.Messages
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsObject, Json, Writes}
import play.twirl.api.Html

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal


@Singleton
class NunjucksRenderer @Inject() (
                                   environment: Environment,
                                   configuration: Configuration,
                                   lifecycle: ApplicationLifecycle,
                                   njkContext: NunjucksContext,
                                   playEC: ExecutionContext
                                 ) {

  private val akkaConfiguration = configuration.get[Configuration]("nunjucks").underlying

  private val actorSystem: ActorSystem = ActorSystem("nunjucks", akkaConfiguration, environment.classLoader)

  lifecycle.addStopHook(() => actorSystem.terminate())

  private val nunjucksEC: ExecutionContext = actorSystem.dispatcher

  private implicit lazy val timeout: Timeout = njkContext.timeout

  private val restartStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: ExceptionInInitializerError => Escalate
    case NonFatal(_)                    => Restart
    case _                              => Escalate
  }

  private val actor = {
    val actor = FromConfig(supervisorStrategy = restartStrategy)
      .props(Props(new NunjucksActor(environment, njkContext)(nunjucksEC)))
    actorSystem.actorOf(actor, "nunjucks-actor")
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
              Future.fromTry(result.map(Html(_)))
          }(playEC)
    }(playEC)
  }

  def render[A : Writes](
                          view: String,
                          context: A,
                          timeout: FiniteDuration = njkContext.timeout
                        )(implicit messages: Messages): Html = {

    Await.result(renderAsync(view, context), timeout)
  }
}
