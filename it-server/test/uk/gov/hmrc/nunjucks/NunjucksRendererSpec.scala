package uk.gov.hmrc.nunjucks

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.nunjucks.models.TestViewModel

class NunjucksRendererSpec extends FreeSpec with MustMatchers
  with ScalaFutures with IntegrationPatience with OptionValues {

  "NunjucksRenderer" - {

    "must render successfully" - {

      val app = new GuiceApplicationBuilder()
      val renderer = app.injector.instanceOf[NunjucksRenderer]
      implicit val request: RequestHeader = FakeRequest()

      "given a valid template" in {

        val result = renderer.render("test.njk").futureValue
        result.toString mustEqual "Hello, !"
      }

      "given a valid template and json context" in {

        val context = Json.obj("name" -> "bar")
        val result = renderer.render("test.njk", context).futureValue
        result.toString mustEqual "Hello, bar!"
      }

      "given a valid template and a model context" in {

        val context = TestViewModel("bar")
        val result = renderer.render("test.njk", context).futureValue
        result.toString mustEqual "Hello, bar!"
      }
    }
  }
}
