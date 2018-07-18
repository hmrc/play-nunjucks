package nunjucks

import com.eclipsesource.v8.{JavaCallback, V8Array, V8Object}
import nunjucks.s2v8.{JsValueWrapper, SNodeJS, SV8Object}
import play.api.i18n.Messages
import play.api.libs.json.{JsString, JsValue, Json, Writes}
import play.api.mvc.Call

class Nunjucks2(delegate: V8Object, context: NunjucksContext)(implicit runtime: SNodeJS) extends SV8Object(delegate) {

  def Nunjucks2(context: NunjucksContext)(implicit runtime: SNodeJS) =
    new Nunjucks2(runtime.require(context.nodeModulesDirectory / "nunjucks").delegate, context)

  /**
    * Add routes helper
    */
  delegate.getRuntime.registerJavaMethod(new JavaCallback {
    override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

      val (router :: route :: params) = (0 until parameters.length).map(parameters.get).toList

      val routerPieces =
        router
          .asInstanceOf[String]
          .split("\\.")

      val routerPackage =
        routerPieces.init.mkString(".")

      val routerClass =
        routerPieces.last

      val field = Class.forName(routerPackage).getField(routerClass)
      val method = field.getType.getMethods.find(_.getName == route).get

      // TODO: release native resources!!
      method.invoke(field.get(null), params: _*).asInstanceOf[Call].url
    }
  }, "route")

  def addGlobal[A : Writes](name: String, value: A): Nunjucks2 = {
    executeFn("addGlobal", name, value)
    this
  }

  def configure(paths: String*): Nunjucks2 = {
    val params = paths.map(s => JsValueWrapper(JsString(s)))
    val configuredInstance = executeObjectFn("configure", params: _*).delegate
    delegate.release()
    new Nunjucks2(configuredInstance, context)
  }

  def render(view: String, params: JsValue, messages: Messages): String = {
    executeStringFn("render", view, params)
  }
}
