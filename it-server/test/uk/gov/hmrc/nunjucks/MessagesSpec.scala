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
import play.api.mvc.RequestHeader
import play.api.test._

class MessagesSpec extends AnyFreeSpec with Matchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "Messages" - {

    val app = new GuiceApplicationBuilder()
    val renderer = app.injector.instanceOf[NunjucksRenderer]
    implicit val request: RequestHeader = FakeRequest()

    "views must" - {

      "be able to be rendered including internationalised text" in {

        val result = renderer.render("messages.njk").futureValue
        result.toString mustEqual "foobar"
      }

      "be able to be rendered including internationalised text with placeholders" in {

        val result = renderer.render("messages-args.njk").futureValue
        result.toString mustEqual "Hello, World!"
      }
    }
  }
}
