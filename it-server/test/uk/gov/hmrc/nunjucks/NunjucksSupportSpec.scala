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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class NunjucksSupportSpec extends AnyFreeSpec with Matchers
  with GuiceOneAppPerSuite with OptionValues {

  val form = Form(
    "foo" -> Forms.text
      .verifying("error.required", _.nonEmpty)
  )

  implicit val request: RequestHeader = FakeRequest()

  "NunjucksSupport" - {

    "must write a form" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form)

      json mustEqual Json.obj(
        "foo"    -> Json.obj("value" -> JsNull),
        "errors" -> Json.arr()
      )
    }

    "must write a bound form" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form.bind(Map("foo" -> "bar")))

      json mustEqual Json.obj(
        "foo"    -> Json.obj("value" -> "bar"),
        "errors" -> Json.arr()
      )
    }

    "must write a form with errors" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form.bind(Map("foo" -> "")))

      json mustEqual Json.obj(
        "foo"    -> Json.obj(
          "value" -> "",
          "error" -> Json.obj(
            "text" -> Messages("error.required")
          )
        ),
        "errors" -> Json.arr(
          Json.obj(
            "text" -> Messages("error.required"),
            "href" -> "#foo"
          )
        )
      )
    }
  }
}
