package nunjucks

import io.apigee.trireme.core.{NodeModule, NodeRuntime}
import org.mozilla.javascript.annotations.JSFunction
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject}
import play.api.{Configuration, Environment}

import scala.io.Source

class NunjucksLoaderModule extends NodeModule {

  override def getModuleName: String = NunjucksLoaderModule.moduleName

  override def registerExports(cx: Context, global: Scriptable, runtime: NodeRuntime): Scriptable = {
    ScriptableObject.defineClass(global, classOf[NunjucksLoader])
    cx.newObject(global, NunjucksLoader.className).asInstanceOf[NunjucksLoader]
  }
}

object NunjucksLoaderModule {

  val moduleName: String = "nunjucks-scala-loader"
}

class NunjucksLoader extends ScriptableObject {

  private def viewPaths: Seq[String] = {

    val context = Context.getCurrentContext

    val configuration =
      context.getThreadLocal("configuration")
        .asInstanceOf[Configuration]

    configuration
      .getOptional[Seq[String]]("nunjucks.viewPaths")
      .getOrElse(Seq.empty)
  }

  override def getClassName: String = NunjucksLoader.className

  @JSFunction
  def getSource(view: String): Scriptable = {

    val context = Context.getCurrentContext

    val environment =
      context.getThreadLocal("environment")
      .asInstanceOf[Environment]

    viewPaths.flatMap(path => environment.resourceAsStream(s"$path/$view"))
      .headOption
      .map(Source.fromInputStream)
      .map(_.mkString)
      .map {
        content =>

          val obj = context.newObject(getParentScope)
          obj.put("path", obj, view)
          obj.put("src", obj, content)
          obj.put("noCache", obj, true)

          obj
      }.orNull
  }
}

object NunjucksLoader {

  val className = "_nunjucksScalaLoader"
}

