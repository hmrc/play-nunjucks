package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.{Form, Forms}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}

import scala.concurrent.ExecutionContext

@Singleton
class QuestionController @Inject() (
                                     renderer: NunjucksRenderer,
                                     cc: ControllerComponents
                                   )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport with NunjucksSupport {

  private val form: Form[String] = Form(
    "value" -> Forms.text.verifying("questionPage.required", _.nonEmpty)
  )

  def get: Action[AnyContent] = Action {
    implicit request =>
      val form2 = request.session.get("postcode").map(form.fill).getOrElse(form)
      Ok(renderer.render("question.njk", Json.obj("form" -> form2)).get)
  }

  def post: Action[AnyContent] = Action {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          BadRequest(renderer.render("question.njk", Json.obj("form" -> errors)).get),
        postcode =>
          Redirect(controllers.routes.QuestionController.get()).addingToSession("postcode" -> postcode)
      )
  }
}
