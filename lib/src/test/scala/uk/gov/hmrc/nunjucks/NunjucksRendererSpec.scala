package uk.gov.hmrc.nunjucks

import org.scalatest.{FreeSpec, MustMatchers, TryValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class NunjucksRendererSpec extends FreeSpec with MustMatchers with TryValues {

  "NunjucksRenderer" - {

    "must render successfully" - {

      "given a valid nunjucks template" in {

        val app = new GuiceApplicationBuilder()
        val renderer = app.injector.instanceOf[NunjucksRenderer]

        implicit val request: RequestHeader = FakeRequest()
        val result = renderer.render("test.njk").get
      }
    }
  }
}
