import nunjucks.s2v8.{JavascriptError, SNodeJS}
import nunjucks.{DefaultNunjucksContext, Nunjucks, NunjucksContext}
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext.Implicits.global

class RouteHelperSpec extends FreeSpec with MustMatchers {

  val context: NunjucksContext = new DefaultNunjucksContext(
    Environment.simple(), Configuration(
      "nunjucks.devDirectory"     -> "nunjucks",
      "nunjucks.libDirectoryName" -> "libs",
      "nunjucks.timeout"          -> 2000,
      "nunjucks.viewPaths"        -> Seq("test/views")
    )
  )

  "`route`" - {

    "must return the reverse route for a given endpoint" in {

      implicit val runtime: SNodeJS = SNodeJS.create()
      val nunjucks = Nunjucks(context)

      val result =
        nunjucks.render("test-routes-helper.njk", Json.obj(), null, FakeRequest()).get

      result mustEqual controllers.routes.QuestionController.get().url

      nunjucks.release()
      runtime.release()
    }

    "must return the reverse route for a given endpoint with arguments" in {

      implicit val runtime: SNodeJS = SNodeJS.create()
      val nunjucks = Nunjucks(context)

      val result =
        nunjucks.render("test-routes-helper-args.njk", Json.obj(), null, FakeRequest()).get

      result mustEqual controllers.routes.TestController.routeWithArgs("foo", 1).url

      nunjucks.release()
      runtime.release()
    }

    "must throw an exception when the route cannot be found" in {

      implicit val runtime: SNodeJS = SNodeJS.create()
      val nunjucks = Nunjucks(context)

      a [JavascriptError] mustBe thrownBy {
        nunjucks.render("test-routes-helper-error.njk", Json.obj(), null, FakeRequest()).get
      }

      nunjucks.release()
      runtime.release()
    }
  }
}
