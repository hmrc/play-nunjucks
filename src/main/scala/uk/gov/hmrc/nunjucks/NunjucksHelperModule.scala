package uk.gov.hmrc.nunjucks

import io.apigee.trireme.core.{ArgUtils, NodeModule, NodeRuntime}
import org.mozilla.javascript.annotations.{JSFunction, JSGetter}
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject, Function => JFunction}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.routing.JavaScriptReverseRouter
import views.html.helper.CSRF

class NunjucksHelperModule extends NodeModule {

  // $COVERAGE-OFF$
  override def getModuleName: String = NunjucksHelperModule.moduleName
  // $COVERAGE-ON$

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

  @JSGetter("request")
  def request(thisObj: ScriptableObject): Scriptable = {

    val cx = Context.getCurrentContext

    val r: RequestHeader = cx.getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val messagesApi: MessagesApi = cx.getThreadLocal("messagesApi")
      .asInstanceOf[MessagesApi]

    val language = messagesApi.preferred(r).lang.language

    val requestObject = cx.newObject(thisObj)
    ScriptableObject.putProperty(requestObject, "language", language)

    requestObject
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
