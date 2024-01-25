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

package uk.gov.hmrc.nunjucks

import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}

class GlobalsSpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues {

  val app                             = new GuiceApplicationBuilder()
  val renderer                        = app.injector.instanceOf[NunjucksRenderer]
  implicit val request: RequestHeader = FakeRequest()

  "globals helper must" - {

    "read a value from the application config file" in {
      val result = renderer.render("config-1.njk").futureValue
      result.toString mustEqual "foobar"
    }

    "read an empty string if a config entry does not exist" in {
      val result = renderer.render("config-2.njk").futureValue
      result.toString mustEqual ""
    }

    "read an empty string if the globals config block is undefined" in {
      val configuration =
        Configuration(Configuration.load(Environment.simple()).underlying.withoutPath("nunjucks.globals"))
      val app2          = new GuiceApplicationBuilder().loadConfig(configuration)
      val renderer      = app2.injector.instanceOf[NunjucksRenderer]
      val result        = renderer.render("config-1.njk").futureValue
      result.toString mustEqual ""
    }
  }
}
