package nunjucks.s2v8

import com.eclipsesource.v8.{V8Array, V8Value}
import play.api.libs.json._

class SV8Array(private[nunjucks] val delegate: V8Array) {

  def toJson(): JsArray = {

    val length = delegate.length

    val list = (0 until length).map {
      index =>
        delegate.getType(index) match {
          case V8Value.V8_ARRAY =>
            delegate.getArray(index).toJson()
          case V8Value.V8_OBJECT =>
            delegate.getObject(index).toJson()
          case V8Value.BOOLEAN =>
            JsBoolean(delegate.getBoolean(index))
          case V8Value.STRING =>
            JsString(delegate.getString(index))
          case V8Value.INTEGER =>
            JsNumber(delegate.getInteger(index))
          case V8Value.DOUBLE =>
            JsNumber(delegate.getDouble(index))
          case V8Value.NULL =>
            JsNull
          case tpe =>
            throw new IllegalArgumentException(s"Cannot convert value of type: $tpe")
        }
    }

    release()

    JsArray(list)
  }

  def release(): Unit =
    delegate.release()
}

