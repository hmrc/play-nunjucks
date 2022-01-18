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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest

class RequestSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "Request" - {

    val app = new GuiceApplicationBuilder()
    val renderer = app.injector.instanceOf[NunjucksRenderer]

    "must include the language when the preferred language is `en`" in {

      val request = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))

      val result = renderer.render("language.njk")(request).futureValue

      result.toString mustEqual "en"
    }

    "must include the language when the preferred language is `cy`" in {

      val request = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))

      val result = renderer.render("language.njk")(request).futureValue

      result.toString mustEqual "cy"
    }
  }
}
