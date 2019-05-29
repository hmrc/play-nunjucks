package nunjucks

import io.apigee.trireme.core.ArgUtils._
import io.apigee.trireme.core.internal.ScriptRunner
import io.apigee.trireme.core.{NodeModule, NodeRuntime, ScriptFuture}
import org.mozilla.javascript.annotations.JSFunction
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject, Function => JFunction}

class NunjucksBootstrapModule extends NodeModule {

  override def getModuleName: String = NunjucksBootstrapModule.moduleName

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
    val returnValue = objArg(args, 0, classOf[Scriptable], true)
    self.future.setModuleResult(returnValue)
  }
}
