package nunjucks.s2v8

import com.eclipsesource.v8.V8
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FreeSpec, MustMatchers, TryValues}
import play.api.libs.json._

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class SV8ObjectSpec extends FreeSpec with MustMatchers with GeneratorDrivenPropertyChecks
  with JsonGenerators with MockFactory with TryValues {

  val genNonEmptyString: Gen[String] = {
    for {
      x  <- Gen.alphaChar
      xs <- Gen.alphaNumStr
    } yield x + xs
  }

  "SV8Object" - {

    ".toJson" - {

      "must return an object" in {

        forAll(arbitrary[JsObject]) {
          json =>

            val nodeJS = SNodeJS.create()
            implicit val runtime: V8 = nodeJS.runtime

            val obj = json.sv8Obj
            obj.toJson() mustEqual json

            nodeJS.release()
        }
      }
    }

    ".executeFn" - {

      "must call the function with the given parameters" in {

        forAll(genNonEmptyString, genNonEmptyString, arbitrary[JsArray]) {
          (fnName, fieldName, args) =>

            val nodeJS = SNodeJS.create()
            val runtime: V8 = nodeJS.runtime
            val fn = mockFunction[JsArray, Unit]

            fn expects args returning Unit once

            nodeJS.registerFn(fnName, fn)

            val obj = runtime.executeObjectScript(
              s"""
                 |(function() {
                 |  return {
                 |    "$fieldName": $fnName
                 |  };
                 |})();
               """.stripMargin
            )

            obj.executeFn(fieldName, args.value.map(JsValueWrapper): _*)

            obj.release()
            nodeJS.release()
        }
      }
    }

    ".executeObjectFn" - {

      "must call the function with the given parameters and return an object" in {

        forAll(genNonEmptyString, arbitrary[JsArray]) {
          (field, args) =>

            val json = Json.obj(
              "args" -> args
            )

            val nodeJS = SNodeJS.create()
            val runtime: V8 = nodeJS.runtime

            val obj = new SV8Object(runtime.executeObjectScript(
              s"""
                |(function () {
                | return {
                |   "$field": function() {
                |     return { args: Array.from(arguments) };
                |   }
                | };
                |})();
              """.stripMargin))

            val result = obj.executeObjectFn(field, args.value.map(JsValueWrapper): _*)

            result.toJson() mustEqual json

            obj.release()
            nodeJS.release()
        }
      }
    }

    ".executeStringFn" - {

      "must call the function with the given parameters and return a string" in {

        forAll(genNonEmptyString, genNonEmptyString, arbitrary[JsArray], arbitrary[String]) {
          (fnName, fieldName, args, string) =>

            val nodeJS = SNodeJS.create()
            val runtime: V8 = nodeJS.runtime
            val fn = mockFunction[JsArray, String]

            fn expects args returning string once

            nodeJS.registerStringFn(fnName, fn)

            val obj = runtime.executeObjectScript(
              s"""
                 |(function() {
                 |  return {
                 |    "$fieldName": $fnName
                 |  };
                 |})();
               """.stripMargin
            )

            val result = obj.executeStringFn(fieldName, args.value.map(JsValueWrapper): _*)
            result mustEqual string

            obj.release()
            nodeJS.release()
        }
      }
    }

    ".executeStringFnViaCallback" - {

      "must call the function with the given parameters and return a string" in {

        forAll(genNonEmptyString, arbitrary[JsArray], arbitrary[String]) {
          (fieldName, args, string) =>

            implicit val nodeJS: SNodeJS = SNodeJS.create()
            val runtime: V8 = nodeJS.runtime

            val obj = runtime.executeObjectScript(
              s"""
                 |(function() {
                 |  return {
                 |    "$fieldName": function() {
                 |      arguments[arguments.length - 1](null, "$string");
                 |    }
                 |  };
                 |})();
               """.stripMargin
            )

            val result = obj.executeStringFnViaCallback(fieldName, args.value.map(JsValueWrapper): _*)
            result.get mustEqual string

            obj.release()
            nodeJS.release()
        }
      }

      "must call the function with the given parameters and return an error" - {

        "when the callback is called with an error object" in {

          forAll(genNonEmptyString, arbitrary[JsArray], arbitrary[String]) {
            (fieldName, args, errorMessage) =>

              implicit val nodeJS: SNodeJS = SNodeJS.create()
              val runtime: V8 = nodeJS.runtime

              val obj = runtime.executeObjectScript(
                s"""
                   |(function() {
                   |  return {
                   |    "$fieldName": function() {
                   |      arguments[arguments.length - 1](new Error("$errorMessage"));
                   |    }
                   |  };
                   |})();
               """.stripMargin
              )

              val result = obj.executeStringFnViaCallback(fieldName, args.value.map(JsValueWrapper): _*)
              result.failed.get.getMessage mustEqual s"""{"message":"$errorMessage"}"""

              obj.release()
              nodeJS.release()
          }
        }

        "when the callback takes too long to be called" in {

          forAll(genNonEmptyString, arbitrary[JsArray], minSuccessful(1)) {
            (fieldName, args) =>

              implicit val nodeJS: SNodeJS = SNodeJS.create()
              val runtime: V8 = nodeJS.runtime

              val obj = runtime.executeObjectScript(
                s"""
                   |(function() {
                   |  return {
                   |    "$fieldName": function() {}
                   |  };
                   |})();
                 """.stripMargin
              )

              val result = obj.executeStringFnViaCallback(fieldName, args.value.map(JsValueWrapper): _*)
              result.failed.get.getMessage must startWith("Timeout out after")
          }
        }
      }
    }
  }
}
