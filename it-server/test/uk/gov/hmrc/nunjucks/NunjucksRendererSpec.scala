/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.nunjucks

import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.{Environment, Mode, PlayException}
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.models.TestViewModel

class NunjucksRendererSpec extends AnyFreeSpec with Matchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "NunjucksRenderer" - {

    val app = new GuiceApplicationBuilder()
    val renderer = app.injector.instanceOf[NunjucksRenderer]
    implicit val request: RequestHeader = FakeRequest()

    "must render successfully" - {

      "given a valid template" in {

        val result = renderer.render("test.njk").futureValue
        result.toString mustEqual "Hello, !"
      }

      "given a valid template and json context" in {

        val context = Json.obj("name" -> "bar")
        val result = renderer.render("test.njk", context).futureValue
        result.toString mustEqual "Hello, bar!"
      }

      "given a valid template and a model context" in {

        val context = TestViewModel("bar")
        val result = renderer.render("test.njk", context).futureValue
        result.toString mustEqual "Hello, bar!"
      }
    }

    "fail to render in non-Dev modes" - {

      "given a template with a syntax error" in {

        whenReady(renderer.render("syntax-error.njk").failed) {
          case exception: PlayException.ExceptionSource =>
            exception.title mustEqual "Template render error"
            exception.line mustEqual 1
            exception.position mustEqual 19
            exception.input mustEqual "Hello, {{ subject }!"
            exception.sourceName mustEqual "syntax-error.njk"
        }
      }

      "given a template with a missing import" in {

        whenReady(renderer.render("import-error.njk").failed) {
          case exception: PlayException =>
            exception.title must include("Template render error: (import-error.njk)")
            exception.description must include("Error: template not found: foo.njk")
        }
      }

      "given a template with a javascript error" in {

        whenReady(renderer.render("javascript-error.njk").failed) {
          case exception: PlayException =>
            exception.title mustEqual "Template render error: (javascript-error.njk)"
            exception.description mustEqual """Error: Unable to call `routes["controllers"]["MissingController"]["get"]`, which is undefined or falsey"""
        }
      }
    }

    "must render a Debug error page in Dev mode" - {

      val dev: Environment = Environment.simple(mode=Mode.Dev)
      val devModeApp = GuiceApplicationBuilder(environment = dev)
      val devModeRenderer = devModeApp.injector.instanceOf[NunjucksRenderer]

      "given a template with a syntax error" in {

        val result: Html = devModeRenderer.render("syntax-error.njk").futureValue
        result.toString() must (
          include ("Template render error")
          and include("syntax-error.njk:1")
          and include ("Hello, {{ subject }")
        )
      }

      "given a template with a missing import" in {

        val result: Html = devModeRenderer.render("import-error.njk").futureValue
        result.toString() must (
          include ("Template render error: (import-error.njk)")
          and include ("Error: template not found: foo.njk")
        )
      }

      "given a template with a javascript error" in {

        val result: Html = devModeRenderer.render("javascript-error.njk").futureValue
        result.toString() must (
          include ("Template render error: (javascript-error.njk)")
          and include ("Error: Unable to call `routes[&quot;controllers&quot;][&quot;MissingController&quot;][&quot;get&quot;]`, which is undefined or falsey")
        )
      }
    }
  }
}
