package nunjucks

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.OWrites
import play.api.mvc.RequestHeader
import play.api.{Configuration, Environment}
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
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


  private val actorSystem: ActorSystem = {

    val akkaConfiguration = configuration.get[Configuration]("nunjucks").underlying

    ActorSystem("nunjucks", akkaConfiguration, environment.classLoader)
  }

  lifecycle.addStopHook(() => actorSystem.terminate())

  private val nunjucksEC: ExecutionContext = actorSystem.dispatcher

  private implicit lazy val timeout: Timeout = njkContext.timeout

  private val actor = {

    val restartStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: ExceptionInInitializerError => Escalate
      case NonFatal(_)                    => Restart
      case _                              => Escalate
    }

    val actor = FromConfig(supervisorStrategy = restartStrategy)
      .props(Props(new NunjucksActor(environment, njkContext, nunjucksEC)))
    actorSystem.actorOf(actor, "nunjucks-actor")
  }

  def renderAsync[A](view: String, context: A)(implicit messages: Messages, request: RequestHeader, writes: OWrites[A]): Future[Html] = {

    val json = writes.writes(context)

    (actor ? NunjucksActor.Render(view, json, messages, request))
      .mapTo[Try[String]]
      .flatMap {
        result =>
          Future.fromTry(result.map(Html(_)))
      }(playEC)
  }

  def render[A](
                view: String,
                context: A,
                timeout: FiniteDuration = njkContext.timeout
               )(implicit messages: Messages, request: RequestHeader, writes: OWrites[A]): Html = {

    Await.result(renderAsync(view, context), timeout)
  }
}
