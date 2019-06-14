package controllers

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}

import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject() (
                                renderer: NunjucksRenderer
                               )(implicit ec: ExecutionContext) extends Controller with NunjucksSupport {

  def routeWithArgs(string: String, int: Int) = Action {
    Ok
  }

  def ok = Action.async {
    implicit request =>
      renderer.render("hello-world.njk", Json.obj("subject" -> "World"))
        .map(Ok(_))
  }
}
