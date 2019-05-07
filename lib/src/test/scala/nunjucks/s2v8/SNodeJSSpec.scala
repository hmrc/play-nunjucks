package nunjucks.s2v8

import better.files.File
import org.scalacheck.Arbitrary.arbitrary
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.language.postfixOps

class SNodeJSSpec extends FreeSpec with MustMatchers with MockFactory with ScalaCheckDrivenPropertyChecks with JsonGenerators {

  "SNodeJS" - {

    ".require" - {

      "must return the imported object" in {

        forAll(arbitrary[JsObject]) {
          value =>

            val tmp = File.newTemporaryDirectory("play-nunjucks-tests")
            val testJS = tmp / "test.js"

            testJS.writeText(s"module.exports = ${Json.stringify(value)};")

            val nodeJS = SNodeJS.create()
            val obj = nodeJS.require(testJS)

            obj.toJson() mustEqual value
            obj.release()

            nodeJS.release()
        }
      }
    }

    "must be able to register and execute void functions on the runtime" in {

      forAll(arbitrary[String], arbitrary[JsArray]) {
        (name, args) =>

          val nodeJS = SNodeJS.create()
          val fn = mockFunction[JsArray, Unit]

          fn expects args returning Unit once

          nodeJS.registerFn(name, fn)
          nodeJS.executeFn(name, args.value.map(JsValueWrapper): _*)
          nodeJS.release()
      }
    }

    "must be able to register and execute string functions on the runtime" in {

      forAll(arbitrary[String], arbitrary[JsArray], arbitrary[String]) {
        (name, args, string) =>

          val nodeJS = SNodeJS.create()
          val fn = mockFunction[JsArray, String]

          fn expects args returning string once

          nodeJS.registerStringFn(name, fn)
          nodeJS.executeStringFn(name, args.value.map(JsValueWrapper): _*)
          nodeJS.release()
      }
    }

    "must be able to register and execute object functions on the runtime" in {

      forAll(arbitrary[String], arbitrary[JsArray], arbitrary[JsObject]) {
        (name, args, obj) =>

          val nodeJS = SNodeJS.create()
          val fn = mockFunction[JsArray, JsObject]

          fn expects args returning obj once

          nodeJS.registerObjectFn(name, fn)
          nodeJS.executeObjectFn(name, args.value.map(JsValueWrapper): _*)
          nodeJS.release()
      }
    }
  }
}
