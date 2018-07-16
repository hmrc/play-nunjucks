package nunjucks

import java.nio.file.Files

import better.files.{File => SFile, _}
import com.eclipsesource.v8._
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Call

class Nunjucks(
                environment: Environment,
                context: NunjucksContext
              ) {

  private val nodeJs = NodeJS.createNodeJS()
  private val runtime = nodeJs.getRuntime

  private val instance = {

    val nunjucks = nodeJs.require((context.nodeModulesDirectory / "nunjucks").toJava)

    val env = {

      val params = {

        val params = new V8Array(runtime)
          .push(environment.resource("views").get.getFile)
          .push(context.libDirectory.pathAsString)

        context.viewsDirectories.foreach {
          dir =>
            params.push(environment.resource(dir).get.getFile)
        }

        new V8Array(runtime).push(params)
      }

      val result = nunjucks.executeObjectFunction("configure", params)
      params.release()
      result
    }

    runtime.registerJavaMethod(new JavaCallback {
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

    Nunjucks.addGlobal(env, "route", runtime.getObject("route"))

    env
  }

  def render(view: String, params: JsValue, messages: Messages): String = {

    setMessages(messages)

    val paramsObject =
      runtime.executeObjectScript(s"(function () { return ${Json.stringify(params)}; })();")

    val paramsArray = new V8Array(runtime)
      .push(view)
      .push(paramsObject)

    val result = instance.executeStringFunction("render", paramsArray)

    paramsArray.release()
    paramsObject.release()
    result
  }

  private def setMessages(messages: Messages): Unit = {

    runtime.registerJavaMethod(new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        // TODO: better errors when the key isn't present
        val (key :: params) =
          (0 until parameters.length).map(parameters.get).toList

        // TODO: release native resources!!
        messages(key.toString, params: _*)
      }
    }, "messages")

    Nunjucks.addGlobal(instance, "messages", runtime.getObject("messages"))
  }

  def release(): Unit = {
    instance.release()
    runtime.release()
  }
}

object Nunjucks {

  private def addGlobal(instance: V8Object, name: String, value: V8Value): Unit = {
    val params = new V8Array(instance.getRuntime)
      .push(name)
      .push(value)
    instance.executeVoidFunction("addGlobal", params)
    params.release()
  }

  private def addGlobal(instance: V8Object, name: String, value: String): Unit = {
    val params = new V8Array(instance.getRuntime)
      .push(name)
      .push(value)
    instance.executeVoidFunction("addGlobal", params)
    params.release()
  }
}