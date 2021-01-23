/*
 * Copyright 2021 HM Revenue & Customs
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

import io.apigee.trireme.core.ArgUtils._
import io.apigee.trireme.core.internal.ScriptRunner
import io.apigee.trireme.core.{NodeModule, NodeRuntime, ScriptFuture}
import org.mozilla.javascript.annotations.JSFunction
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject, Function => JFunction}

class NunjucksBootstrapModule extends NodeModule {

  // $COVERAGE-OFF$
  override def getModuleName: String = NunjucksBootstrapModule.moduleName
  // $COVERAGE-ON$

  override def registerExports(cx: Context, global: Scriptable, runtime: NodeRuntime): Scriptable = {

    ScriptableObject.defineClass(global, classOf[NunjucksBootstrapModuleImpl])

    val future = runtime.asInstanceOf[ScriptRunner].getFuture
    val exports = cx.newObject(global, NunjucksBootstrapModuleImpl.className).asInstanceOf[NunjucksBootstrapModuleImpl]

    exports.future = future
    exports
  }
}

object NunjucksBootstrapModule {

  val moduleName: String = "nunjucks-bootstrap"
}

class NunjucksBootstrapModuleImpl extends ScriptableObject {

  override def getClassName: String = NunjucksBootstrapModuleImpl.className

  var future: ScriptFuture = null
}

object NunjucksBootstrapModuleImpl {

  val className = "_callbackModuleClass"

  @JSFunction
  def setReturnValue(cx: Context, thisObj: Scriptable, args: Array[AnyRef], func: JFunction): Unit = {
    val self = thisObj.asInstanceOf[NunjucksBootstrapModuleImpl]
    val returnValue = objArg(cx, thisObj, args, 0, classOf[Scriptable], true)
    self.future.setModuleResult(returnValue)
  }
}
