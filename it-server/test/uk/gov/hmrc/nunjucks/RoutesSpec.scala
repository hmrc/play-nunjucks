/*
 * Copyright 2021 HM Revenue & Customs
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

import java.net.URLClassLoader

import better.files._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.{Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RoutesSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "Routes" - {

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    def buildRenderer(environment: Environment): NunjucksRenderer = {
      lazy val app = GuiceApplicationBuilder(environment).build()

      // force instantiation of the router, needed for correct prefixing of sub-routers in Play 2.7+
      // as a result of this: https://github.com/playframework/playframework/pull/10030
      route(app, request)

      app.injector.instanceOf[NunjucksRenderer]
    }

    "production routes helper" - {
      lazy val renderer = buildRenderer(Environment.simple(mode = Mode.Prod))
      aRoutesHelper(renderer)
    }

    "development routes helper" - {

      // here we're using an empty classloader to simulate
      // the fact that in development mode the application
      // classloader is different from the one used to load
      // library classes
      val classLoader = new URLClassLoader(Array.empty)

      val environment = Environment(
        rootPath = File(".").toJava,
        classLoader = classLoader,
        mode = Mode.Dev
      )

      lazy val renderer = buildRenderer(environment)

      aRoutesHelper(renderer)
    }

    def aRoutesHelper(renderer: NunjucksRenderer): Unit = {

      "must be able to be rendered" in {

        val result = renderer.render("routes.njk").futureValue
        result.toString mustEqual "/ok"
      }

      "must be able to be rendered with parameters" in {

        val result = renderer.render("routes-args.njk").futureValue
        result.toString mustEqual "/route-with-args/foo/1337"
      }

      "must be able to be rendered when the route is from a sub router" in {

        val result = renderer.render("routes-sub.njk").futureValue
        result.toString mustEqual "/sub/ok"
      }
    }
  }
}
