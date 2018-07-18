package nunjucks.s2v8

import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json._

trait JsonGenerators {

  implicit def noShrink[A]: Shrink[A] = Shrink.shrinkAny[A]

  implicit val arbitraryString: Arbitrary[String] =
    Arbitrary {
      Gen.alphaNumStr
    }

  implicit val arbitraryJsNumber: Arbitrary[JsNumber] =
    Arbitrary {
      Gen.oneOf(
        arbitrary[Int].map(JsNumber(_)),
        arbitrary[Double].map(JsNumber(_))
      )
    }

  implicit val arbitraryJsString: Arbitrary[JsString] =
    Arbitrary {
      arbitrary[String].map(JsString)
    }

  implicit val arbitraryJsBoolean: Arbitrary[JsBoolean] =
    Arbitrary {
      arbitrary[Boolean].map(JsBoolean)
    }

  implicit lazy val arbitraryJsValue: Arbitrary[JsValue] =
    Arbitrary {
      Gen.frequency(
        1 -> arbitrary[JsObject],
        1 -> arbitrary[JsArray],
        3 -> arbitrary[JsBoolean],
        3 -> arbitrary[JsString],
        3 -> arbitrary[JsNumber]
      )
    }

  implicit lazy val arbitraryJsArray: Arbitrary[JsArray] =
    Arbitrary {
      for {
        size <- Gen.chooseNum(0, 10)
        list <- Gen.listOfN(size, arbitrary[JsValue])
      } yield JsArray(list)
    }

  implicit lazy val arbitraryJsObject: Arbitrary[JsObject] =
    Arbitrary {
      for {
        size <- Gen.chooseNum(0, 10)
        obj <- Gen.listOfN(size,
          for {
            key   <- Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)
            value <- arbitrary[JsValue]
          } yield key -> value).map(s => JsObject(s.toMap))
      } yield obj
    }
}
