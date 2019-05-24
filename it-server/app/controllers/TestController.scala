package controllers

import javax.inject.Inject
import nunjucks.{NunjucksRenderer, NunjucksSupport}
import play.api.Environment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class TestController @Inject() (
                                 cc: ControllerComponents,
                                 override val messagesApi: MessagesApi,
                                 renderer: NunjucksRenderer,
                                 environment: Environment
                               )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport with NunjucksSupport {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }

  def ok = Action.async {
    implicit request =>
      Future.fromTry(renderer.render("hello-world.njk", Json.obj("subject" -> "World")))
        .map(Ok(_))
  }
}
