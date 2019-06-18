package uk.gov.hmrc.nunjucks

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.filters.csrf.CSRF

import scala.xml.XML

class CSRFSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "CSRF" - {

    lazy val app = new GuiceApplicationBuilder()

    "must render a hidden input with the relevant CSRF token" in {

      val request = FakeRequest()
        .withCSRFToken

      val token = request.attrs.get(CSRF.Token.InfoAttr).value.toToken

      val renderer = app.injector.instanceOf[NunjucksRenderer]
      val result = renderer.render("csrf.njk")(request).futureValue

      val xml = XML.loadString(result.toString)
      xml mustEqual <input value={token.value} name={token.name} type="hidden"/>
    }
  }
}
