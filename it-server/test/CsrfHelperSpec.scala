import nunjucks.{DefaultNunjucksContext, Nunjucks, NunjucksContext}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.CSRFTokenHelper._
import play.api.{Configuration, Environment}
import play.filters.csrf.CSRF.Token

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.XML

class CsrfHelperSpec extends FreeSpec with MustMatchers with GuiceOneAppPerSuite with TryValues with OptionValues {

  val context: NunjucksContext = new DefaultNunjucksContext(
    Environment.simple(), Configuration(
      "nunjucks.devDirectory"     -> "nunjucks",
      "nunjucks.libDirectoryName" -> "libs",
      "nunjucks.timeout"          -> 2000,
      "nunjucks.viewPaths"        -> Seq("test/views")
    )
  )

  "`csrf`" - {

    "must return the CSRF field for the current request" in {

      val nunjucks = Nunjucks(context)
      val messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
      val request = FakeRequest().withCSRFToken
      val token = request.attrs.get(Token.InfoAttr).value.toToken

      val result =
        XML.loadString(nunjucks.render("test-csrf-helper.njk", Json.obj(), messages, request).success.value)

      result mustEqual <input value={token.value} name="csrfToken" type="hidden"/>

      nunjucks.release()
    }
  }
}
