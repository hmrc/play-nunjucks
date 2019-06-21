package uk.gov.hmrc.nunjucks

import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest

class NunjucksSupportSpec extends FreeSpec with MustMatchers
  with GuiceOneAppPerSuite with OptionValues {

  val form = Form(
    "foo" -> Forms.text
      .verifying("error.required", _.nonEmpty)
  )

  implicit val request: RequestHeader = FakeRequest()

  "NunjucksSupport" - {

    "must write a form" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form)

      json mustEqual Json.obj(
        "foo"    -> Json.obj("value" -> JsNull),
        "errors" -> Json.arr()
      )
    }

    "must write a bound form" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form.bind(Map("foo" -> "bar")))

      json mustEqual Json.obj(
        "foo"    -> Json.obj("value" -> "bar"),
        "errors" -> Json.arr()
      )
    }

    "must write a form with errors" in new NunjucksSupport with I18nSupport {

      override lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      val json = Json.toJson(form.bind(Map("foo" -> "")))

      json mustEqual Json.obj(
        "foo"    -> Json.obj(
          "value" -> "",
          "error" -> Json.obj(
            "text" -> Messages("error.required")
          )
        ),
        "errors" -> Json.arr(
          Json.obj(
            "text" -> Messages("error.required"),
            "href" -> "#foo"
          )
        )
      )
    }
  }
}
