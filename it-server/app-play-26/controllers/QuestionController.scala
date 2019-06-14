package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.{Form, Forms}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QuestionController @Inject() (
                                     renderer: NunjucksRenderer,
                                     cc: ControllerComponents
                                   )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport with NunjucksSupport {

  private val form: Form[String] = Form(
    "value" -> Forms.text.verifying("questionPage.required", _.nonEmpty)
  )

  def get: Action[AnyContent] = Action.async {
    implicit request =>

      val boundForm = request.session.get("postcode")
        .map(form.fill)
        .getOrElse(form)

      renderer.render("question.njk", Json.obj("form" -> boundForm))
        .map(Ok(_))
  }

  def post: Action[AnyContent] = Action.async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          renderer.render("question.njk", Json.obj("form" -> errors))
            .map(BadRequest(_)),
        postcode =>
          Future.successful {
            Redirect(controllers.routes.QuestionController.get())
              .addingToSession("postcode" -> postcode)
          }
      )
  }
}
