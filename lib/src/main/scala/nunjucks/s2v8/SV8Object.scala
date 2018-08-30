package nunjucks.s2v8

import java.util.concurrent.atomic.AtomicReference

import com.eclipsesource.v8._
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.{Failure, Success, Try}

class SV8Object(private[nunjucks] val delegate: V8Object) {

  private val logger = Logger(this.getClass)

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

  def executeStringFnViaCallback(name: String, args: JsValueWrapper*)
                                (implicit nodeJS: SNodeJS,
                                 ec: ExecutionContext,
                                 timeout: FiniteDuration = 1.second
                                ): Try[String] = {

    val result = new AtomicReference[Try[String]]

    val callback = new V8Function(runtime, new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        val error = parameters.getObject(0)

        if (error != null) {
          result.set(Failure(JavascriptError(error)))
        } else {
          val string = parameters.getString(1)
          result.set(Success(string))
        }

        null
      }
    })

    val params = args.toJsArray.sv8Arr.delegate
      .push(callback.delegate)

    delegate.executeVoidFunction(name, params)

    val timeoutPromise = Future {
      Thread.sleep(timeout.toMillis)
      result.compareAndSet(null, Failure(new TimeoutException(s"Timeout out after ${timeout.toString}")))
    }

    while (result.get == null) {
      logger.trace("running node event loop")
      nodeJS.handleMessage()
    }

    Future.firstCompletedOf(Seq(timeoutPromise, Future.successful({})))

    params.release()
    callback.release()

    result.get
  }

  def release(): Unit = {
    delegate.release()
  }
}
