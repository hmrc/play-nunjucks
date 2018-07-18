package nunjucks.s2v8

import better.files._
import com.eclipsesource.v8._
import play.api.libs.json.{JsArray, JsObject, Reads, Writes}

class SNodeJS(nodeJS: NodeJS) {

  implicit lazy val runtime: V8 = nodeJS.getRuntime

  def release(): Unit = {
    nodeJS.release()
    runtime.release()
  }

  def require(file: File): SV8Object =
    nodeJS.require(file.toJava)

  def registerFn(name: String, fn: JsArray => Unit): Unit = {
    runtime.registerJavaMethod(new JavaVoidCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): Unit =
        fn(parameters.toJson())
    }, name)
  }

  def executeFn(name: String, args: JsValueWrapper*): Unit = {
    val v8Args = args.toJsArray.sv8Arr.delegate
    runtime.executeVoidFunction(name, v8Args)
    v8Args.release()
  }

  def registerStringFn(name: String, fn: JsArray => String): Unit = {
    runtime.registerJavaMethod(new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef =
        fn(parameters.toJson())
    }, name)
  }

  def registerStringFn[A : Reads](name: String, fn: (A, Seq[AnyRef]) => String): Unit = {
    runtime.registerJavaMethod(new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        val a = parameters.getObject(0).toJson().as[A]
        val rest = (1 until parameters.length).map(parameters.get)

        fn()
      }
    }, name)
  }

  def executeStringFn(name: String, args: JsValueWrapper*): String = {
    val v8Args = args.toJsArray.sv8Arr.delegate
    val result = runtime.executeStringFunction(name, v8Args)
    v8Args.release()
    result
  }

  def registerObjectFn(name: String, fn: JsArray => JsObject): Unit = {
    runtime.registerJavaMethod(new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef =
        fn(parameters.toJson()).sv8Obj.delegate
    }, name)
  }

  def executeObjectFn(name: String, args: JsValueWrapper*): JsObject = {
    val v8Args = args.toJsArray.sv8Arr.delegate
    val result = runtime.executeObjectFunction(name, v8Args)
    v8Args.release()
    result.toJson()
  }
}

object SNodeJS {

  def create(): SNodeJS =
    new SNodeJS(NodeJS.createNodeJS())
}
