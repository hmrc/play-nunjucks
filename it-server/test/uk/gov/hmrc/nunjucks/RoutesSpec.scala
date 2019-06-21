package uk.gov.hmrc.nunjucks

import java.net.URLClassLoader

import better.files._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.{Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class RoutesSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "Routes" - {

    implicit val request: RequestHeader = FakeRequest()

    "production routes helper" - {

      lazy val app = GuiceApplicationBuilder(Environment.simple(mode = Mode.Prod))
      lazy val renderer = app.injector.instanceOf[NunjucksRenderer]

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

      lazy val app = GuiceApplicationBuilder(environment)
      lazy val renderer = app.injector.instanceOf[NunjucksRenderer]

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
