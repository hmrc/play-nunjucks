/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport
    with NunjucksSupport {

  private val form: Form[String] = Form(
    "value" -> Forms.text.verifying("questionPage.required", _.nonEmpty)
  )

  def get: Action[AnyContent] = Action.async { implicit request =>
    val boundForm = request.session
      .get("postcode")
      .map(form.fill)
      .getOrElse(form)

    renderer
      .render("question.njk", Json.obj("form" -> boundForm))
      .map(Ok(_))
  }

  def post: Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        errors =>
          renderer
            .render("question.njk", Json.obj("form" -> errors))
            .map(BadRequest(_)),
        postcode =>
          Future.successful {
            Redirect(controllers.routes.QuestionController.get)
              .addingToSession("postcode" -> postcode)
          }
      )
  }
}
