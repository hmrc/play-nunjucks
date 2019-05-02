package nunjucks

import com.eclipsesource.v8._
import play.api.libs.json._

import scala.language.implicitConversions

package object s2v8 {

  case class JsValueWrapper(field: JsValue)

  implicit def toJsFieldJsValueWrapper[T](field: T)(implicit w: Writes[T]): JsValueWrapper =
    JsValueWrapper(w.writes(field))

  implicit class RichJsValueWrapperSeq(seq: Seq[JsValueWrapper]) {
    def toJsArray: JsArray =
      JsArray(seq.map(_.field))
  }

  implicit def toSV8Array(arr: V8Array): SV8Array =
    new SV8Array(arr)

  implicit def toSV8Object(obj: V8Object): SV8Object =
    new SV8Object(obj)

  implicit class RichJsArray(arr: JsArray) {
    def sv8Arr(implicit runtime: V8): SV8Array = {
      val json = Json.stringify(arr)
      // TODO: can we remove the function here?
      new SV8Array(runtime.executeArrayScript(s"(function () { return ($json); })();"))
    }
  }

  implicit class RichJsObj(obj: JsObject) {
    def sv8Obj(implicit runtime: V8): SV8Object = {
      val json = Json.stringify(obj)
      // TODO: can we remove the function here?
      new SV8Object(runtime.executeObjectScript(s"(function () { return ($json); })();"))
    }
  }
}
