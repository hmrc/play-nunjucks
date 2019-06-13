package uk.gov.hmrc.nunjucks

import io.apigee.trireme.core.{ArgUtils, NodeModule, NodeRuntime}
import org.mozilla.javascript.annotations.{JSFunction, JSGetter}
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject, Function => JFunction}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.routing.JavaScriptReverseRouter
import views.html.helper.CSRF

class NunjucksHelperModule extends NodeModule {

  override def getModuleName: String = NunjucksHelperModule.moduleName

  override def registerExports(cx: Context, global: Scriptable, runtime: NodeRuntime): Scriptable = {

    ScriptableObject.defineClass(global, classOf[NunjucksHelper])
    cx.newObject(global, NunjucksHelper.className)
  }
}

object NunjucksHelperModule {

  val moduleName = "nunjucks-helpers"
}

class NunjucksHelper extends ScriptableObject {

  override def getClassName: String = NunjucksHelper.className
}

object NunjucksHelper {

  val className = "_nunjucksHelper"

  @JSFunction
  def messages(cx: Context, thisObj: Scriptable, args: Array[AnyRef], fn: JFunction): String = {

    val request: RequestHeader = cx.getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val messagesApi: MessagesApi = cx.getThreadLocal("messagesApi")
      .asInstanceOf[MessagesApi]

    val key = ArgUtils.stringArg(args, 0)

    messagesApi.preferred(request)(key, args.tail: _*)
  }

  @JSFunction
  def csrf(cx: Context, thisObj: Scriptable, args: Array[AnyRef], fn: JFunction): String = {

    val request: RequestHeader = cx.getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    CSRF.formField(request).toString
  }

  @JSGetter("routes")
  def routes(thisObj: ScriptableObject): Scriptable = {

    val cx = Context.getCurrentContext

    val request: RequestHeader = cx.getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val reverseRoutes: NunjucksRoutesHelper = cx.getThreadLocal("reverseRoutes")
      .asInstanceOf[NunjucksRoutesHelper]

    val script = JavaScriptReverseRouter("routes")(reverseRoutes.routes: _*)(request).toString

    cx.evaluateString(thisObj.getParentScope, s"(function () { $script; return routes; })();", "routes", 0, null)
      .asInstanceOf[Scriptable]
  }
}