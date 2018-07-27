import nunjucks.{DefaultNunjucksContext, Nunjucks, NunjucksContext}
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext.Implicits.global

class MessagesHelperSpec extends FreeSpec with MustMatchers with GuiceOneAppPerSuite {

  val context: NunjucksContext = new DefaultNunjucksContext(
    Environment.simple(), Configuration(
      "nunjucks.devDirectory"     -> "nunjucks",
      "nunjucks.libDirectoryName" -> "libs",
      "nunjucks.viewPaths"        -> Seq("test/views")
    )
  )

  "`messages`" - {

    "must return the message at the given key" in {

      val nunjucks = Nunjucks(context)
      val messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

      val result =
        nunjucks.render("test-messages-helper.njk", Json.obj(), messages).get

      result mustEqual "foobar"

      nunjucks.release()
    }

    "must return the message at the given key with args" in {

      val nunjucks = Nunjucks(context)
      val messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

      val result =
        nunjucks.render("test-messages-helper-args.njk", Json.obj("name" -> "World"), messages).get

      result mustEqual "Hello, World!"

      nunjucks.release()
    }
  }
}
