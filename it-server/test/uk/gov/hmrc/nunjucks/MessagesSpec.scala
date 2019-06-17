package uk.gov.hmrc.nunjucks

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test._

class MessagesSpec extends FreeSpec with MustMatchers
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
