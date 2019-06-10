package controllers

import com.google.inject.{Inject, Singleton}
import play.api.data.{Form, Forms}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}

@Singleton
class QuestionController @Inject() (
                                     renderer: NunjucksRenderer,
                                     val messagesApi: MessagesApi
                                   ) extends Controller with I18nSupport with NunjucksSupport {

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
