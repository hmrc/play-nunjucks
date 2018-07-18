package nunjucks.s2v8

import com.eclipsesource.v8.{V8, V8Object, V8Value}
import play.api.libs.json._

class SV8Object(private[nunjucks] val delegate: V8Object) {

  private implicit val runtime: V8 = delegate.getRuntime

  def toJson(): JsObject = {

    val entries: Seq[(String, JsValue)] = delegate.getKeys.map {
      key =>
        key -> {
          delegate.getType(key) match {
            case V8Value.V8_ARRAY =>
              delegate.getArray(key).toJson()
            case V8Value.V8_OBJECT =>
              delegate.getObject(key).toJson()
            case V8Value.BOOLEAN =>
              JsBoolean(delegate.getBoolean(key))
            case V8Value.STRING =>
              JsString(delegate.getString(key))
            case V8Value.INTEGER =>
              JsNumber(delegate.getInteger(key))
            case V8Value.DOUBLE =>
              JsNumber(delegate.getDouble(key))
            case V8Value.NULL =>
              JsNull
            case tpe =>
              throw new IllegalArgumentException(s"Cannot convert value of type: $tpe")
          }
        }
    }

    release()

    JsObject(entries)
  }

  def executeObjectFn(name: String, json: JsValueWrapper*): SV8Object = {
    val params = json.toJsArray.sv8Arr.delegate
    val result = delegate.executeObjectFunction(name, params)
    params.release()
    result
  }

  def executeStringFn(name: String, json: JsValueWrapper*): String = {
    val params = json.toJsArray.sv8Arr.delegate
    val result = delegate.executeStringFunction(name, params)
    params.release()
    result
  }

  def executeFn(name: String, json: JsValueWrapper*): Unit = {
    val params = json.toJsArray.sv8Arr.delegate
    delegate.executeVoidFunction(name, params)
    params.release()
  }

  def release(): Unit = {
    delegate.release()
  }
}
