package uk.gov.hmrc.nunjucks

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRF

import scala.xml.XML



class CSRFSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "CSRF" - {

    lazy val app = new GuiceApplicationBuilder().build()

    "must render a hidden input with the relevant CSRF token" in {

      running(app) {

        val token = CSRF.Token(name = "csrfTokenName", value = "csrfTokenValue")
        val request = FakeRequest()
          .withTag(CSRF.Token.NameRequestTag, token.name)
          .withTag(CSRF.Token.RequestTag, token.value)

        val renderer = app.injector.instanceOf[NunjucksRenderer]
        val result = renderer.render("csrf.njk")(request).futureValue

        val xml = XML.loadString(result.toString)
        xml mustEqual <input value="csrfTokenValue" name="csrfTokenName" type="hidden"/>
      }
    }
  }
}
