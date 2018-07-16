package controllers

import com.google.inject.{Inject, Singleton}
import nunjucks.{NunjucksRenderer, NunjucksSupport}
import play.api.data.{Form, Forms}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class QuestionController @Inject() (
                                     val renderer: NunjucksRenderer,
                                     val messagesApi: MessagesApi
                                   )(implicit ec: ExecutionContext) extends Controller with I18nSupport with NunjucksSupport {

  private val form: Form[String] = Form(
    "value" -> Forms.text.verifying("questionPage.required", _.nonEmpty)
  )

  def get: Action[AnyContent] = Action.async {
    implicit request =>
      val form2 = request.session.get("postcode").map(form.fill).getOrElse(form)
      renderer.render("question.njk", Json.obj("form" -> form)).map(Ok(_))
  }

  def post: Action[AnyContent] = Action.async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          renderer.render("question.njk", Json.obj("form" -> errors)).map(BadRequest(_)),
        postcode =>
          Future.successful(Redirect(controllers.routes.QuestionController.get).addingToSession("postcode" -> postcode))
      )
  }
}
