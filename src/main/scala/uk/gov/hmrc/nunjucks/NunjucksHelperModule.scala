/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nunjucks

import io.apigee.trireme.core.{ArgUtils, NodeModule, NodeRuntime}
import org.mozilla.javascript.annotations.{JSFunction, JSGetter}
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject, Function => JFunction}
import play.api.i18n.MessagesApi
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

  private val routesInitScript =
    """
      |var routes = {};
      |
      |function isObject(item) {
      |  return (item && typeof item === 'object' && !Array.isArray(item));
      |}
      |
      |function mergeDeep(target, source) {
      |
      |  for (var key in source) {
      |    if (isObject(source[key])) {
      |      if (!target[key]) {
      |        var obj = {};
      |        obj[key] = {};
      |        Object.assign(target, obj);
      |      }
      |      mergeDeep(target[key], source[key]);
      |    } else {
      |      var obj = {};
      |      obj[key] = source[key];
      |      Object.assign(target, obj);
      |    }
      |  }
      |
      |  return target;
      |}
      |""".stripMargin

  @JSFunction
  def messages(cx: Context, thisObj: Scriptable, args: Array[AnyRef], fn: JFunction): String = {

    val request: RequestHeader = cx
      .getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val messagesApi: MessagesApi = cx
      .getThreadLocal("messagesApi")
      .asInstanceOf[MessagesApi]

    val key = ArgUtils.stringArg(args, 0)

    messagesApi.preferred(request)(key, args.tail: _*)
  }

  @JSFunction
  def csrf(cx: Context, thisObj: Scriptable, args: Array[AnyRef], fn: JFunction): String = {

    val request: RequestHeader = cx
      .getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    CSRF.formField(request).toString
  }

  @JSGetter("request")
  def request(thisObj: ScriptableObject): Scriptable = {

    val cx = Context.getCurrentContext

    val r: RequestHeader = cx
      .getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val messagesApi: MessagesApi = cx
      .getThreadLocal("messagesApi")
      .asInstanceOf[MessagesApi]

    val language = messagesApi.preferred(r).lang.language

    val requestObject = cx.newObject(thisObj)
    ScriptableObject.putProperty(requestObject, "language", language)

    requestObject
  }

  @JSGetter("routes")
  def routes(thisObj: ScriptableObject): Scriptable = {

    val cx = Context.getCurrentContext

    val request: RequestHeader = cx
      .getThreadLocal("request")
      .asInstanceOf[RequestHeader]

    val routesHelper: NunjucksRoutesHelper = cx
      .getThreadLocal("reverseRoutes")
      .asInstanceOf[NunjucksRoutesHelper]

    val configuration: NunjucksConfiguration = cx
      .getThreadLocal("configuration")
      .asInstanceOf[NunjucksConfiguration]

    val scope = {
      val context = Context.enter()
      val scope   = context.initSafeStandardObjects(null, true)
      Context.exit()
      scope
    }

    cx.evaluateString(scope, routesInitScript, "init_routes", 0, null)

    // we need to batch routes as trireme fails to run the script if it's too large
    routesHelper.routes.sliding(configuration.routesBatchSize, configuration.routesBatchSize).foreach { batch =>
      val script = JavaScriptReverseRouter("batch")(batch: _*)(request).toString
      cx.evaluateString(scope, s"(function () { $script; mergeDeep(routes, batch); })();", "batch", 0, null);
    }

    cx.evaluateString(scope, "routes", "routes", 0, null)
      .asInstanceOf[Scriptable]
  }
}
