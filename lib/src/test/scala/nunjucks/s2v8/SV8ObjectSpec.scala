package nunjucks.s2v8

import com.eclipsesource.v8.V8
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json._

import scala.language.postfixOps

class SV8ObjectSpec extends FreeSpec with MustMatchers with GeneratorDrivenPropertyChecks with JsonGenerators with MockFactory {

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

            fn expects args returning () once

            nodeJS.registerFn(fnName, fn)

            val obj = runtime.executeObjectScript(
              s"""
                 |(function() {
                 |  return {
                 |    "$fieldName": $fnName
                 |  }
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
                 |  }
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
  }
}
