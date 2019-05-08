package nunjucks

import nunjucks.s2v8.JavascriptError
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext.Implicits.global

class NunjucksSpec extends FreeSpec with MustMatchers {

  val context: NunjucksContext = new DefaultNunjucksContext(
    Environment.simple(), Configuration(
      "nunjucks.devDirectory"     -> "nunjucks",
      "nunjucks.libDirectoryName" -> "libs",
      "nunjucks.timeout"          -> 2000,
      "nunjucks.viewPaths"        -> Seq("views")
    )
  )

  "Nunjucks" - {

    "must render a view" in {

      val nunjucks = Nunjucks(context)

      val result = nunjucks.render("test.njk", Json.obj("name" -> "World"), null, FakeRequest()).get
      result mustEqual "Hello, World!"

      nunjucks.release()
    }

    "must return an error when imports are not found" in {

      val nunjucks = Nunjucks(context)

      a[RuntimeException] mustBe thrownBy {
        nunjucks.render("test-import-not-found.njk", Json.obj("name" -> "World"), null, FakeRequest()).get
      }

      nunjucks.release()
    }
  }
}
