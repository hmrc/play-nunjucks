package uk.gov.hmrc.nunjucks

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest

class LanguageSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "Language" - {

    val app = new GuiceApplicationBuilder()
    val renderer = app.injector.instanceOf[NunjucksRenderer]

    "must be correct when the preferred language is `en`" in {

      val request = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))

      val result = renderer.render("language.njk")(request).futureValue

      result.toString mustEqual "en"
    }

    "must be correct when the preferred language is `cy`" in {

      val request = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))

      val result = renderer.render("language.njk")(request).futureValue

      result.toString mustEqual "cy"
    }
  }
}
