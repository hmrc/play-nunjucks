package uk.gov.hmrc.nunjucks


import java.nio.file.Files

import better.files.File
import io.apigee.trireme.core.NodeModule
import javax.inject.{Inject, Singleton}
import org.mozilla.javascript.json.JsonParser
import org.mozilla.javascript.{Context, JavaScriptException, Function => JFunction}
import org.webjars.WebJarExtractor
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.mvc.RequestHeader
import play.api.{Configuration, Environment, PlayException}
import play.twirl.api.Html

import scala.io.Source
import scala.util.{Failure, Success, Try}

@Singleton
class NunjucksRenderer @Inject() (
                                   setup: NunjucksSetup,
                                   configuration: Configuration,
                                   environment: Environment,
                                   reverseRoutes: NunjucksRoutesHelper,
                                   messagesApi: MessagesApi
                                 ) {

  private val render = {

    val customModules: Map[String, NodeModule] = Map(
      NunjucksBootstrapModule.moduleName -> new NunjucksBootstrapModule(),
      NunjucksLoaderModule.moduleName    -> new NunjucksLoaderModule(),
      NunjucksHelperModule.moduleName    -> new NunjucksHelperModule()
    )

    val env = new PlayNodeEnvironment(customModules)
    env.setDefaultNodeVersion("0.12")

    val libPaths = ("" :: configuration
      .getOptional[Seq[String]]("nunjucks.libPaths")
      .getOrElse(Nil).toList)
      .map {
        dir =>
          (setup.libDir / dir).pathAsString
      }

    val script = env.createScript("[eval]", setup.script.toJava, libPaths.toArray)

    script.execute().getModuleResult().asInstanceOf[JFunction]
  }

  private val scope = {

    val context = Context.enter()
    val scope = context.initSafeStandardObjects(null, true)
    scope.sealObject()
    Context.exit()

    scope
  }

  def render(template: String, ctx: JsObject)(implicit request: RequestHeader): Try[Html] = {

    val context = Context.enter()

    val result = Try {

      context.putThreadLocal("configuration", configuration)
      context.putThreadLocal("environment", environment)
      context.putThreadLocal("messagesApi", messagesApi)
      context.putThreadLocal("reverseRoutes", reverseRoutes)
      context.putThreadLocal("request", request)

      val obj = new JsonParser(context, scope).parseValue(Json.stringify(ctx))

      render.call(context, scope, null, Array(template, obj)).asInstanceOf[String]
    }.transform(Success.apply, toPlayException)

    context.removeThreadLocal("configuration")
    context.removeThreadLocal("environment")
    context.removeThreadLocal("messagesApi")
    context.removeThreadLocal("reverseRoutes")
    context.removeThreadLocal("request")

    Context.exit()

    result.map(Html.apply)
  }

  def render(template: String)(implicit request: RequestHeader): Try[Html] =
    render(template, Json.obj())

  def render[A](template: String, ctx: A)(implicit request: RequestHeader, writes: OWrites[A]): Try[Html] =
    render(template, writes.writes(ctx))

  private val TemplateError = """Template render error: \((.*)\) \[Line (\d+), Column (\d+)\]""".r

  private def toPlayException[A](e: Throwable): Failure[A] = {
    Failure {
      e match {
        case e: JavaScriptException =>

          val (first, stack) = e.details.splitAt(e.details.indexOf("\n"))

          first match {
            case TemplateError(file, lpos, cpos) =>

              val viewPaths =
                "" :: configuration.getOptional[Seq[String]]("nunjucks.viewPaths").getOrElse(Seq.empty).toList

              val source = viewPaths.flatMap(path => environment.resourceAsStream(s"$path/$file"))
                .headOption
                .map(Source.fromInputStream)
                .map(_.mkString)
                .getOrElse("")

              new PlayException.ExceptionSource("Nunjucks Exception", stack.trim) {

                override def line(): Integer = lpos.toInt

                override def position(): Integer = cpos.toInt

                override def input(): String = source

                override def sourceName(): String = file
              }
            case _ =>
              e
          }
        case e => e
      }
    }
  }
}

@Singleton
class NunjucksSetup @Inject() (
                                configuration: Configuration,
                                environment: Environment
                              ) {

  val (nodeModulesDir, workingDir, script, libDir) = {

    val tmpDir = File.newTemporaryDirectory("nunjucks").deleteOnExit()

    val nodeModulesTarName = "nodeModules.tar"
    val tarStream = environment.resourceAsStream(nodeModulesTarName).get
    val nodeModulesTar = File(tmpDir.path) / nodeModulesTarName
    val nodeModulesDir = tmpDir / "node_modules"
    Files.copy(tarStream, nodeModulesTar.path)
    nodeModulesTar.unzipTo(tmpDir)
    Files.delete(nodeModulesTar.path)

    val scriptFile = File(tmpDir.path) / "nunjucks-bootstrap.js"
    Files.copy(environment.resourceAsStream("uk/gov/hmrc/nunjucks/nunjucks-bootstrap.js").get, scriptFile.path)

    val libDir = tmpDir / "lib"
    val extractor = new WebJarExtractor(environment.classLoader)
    extractor.extractAllWebJarsTo(libDir.toJava)

    (nodeModulesDir, tmpDir, scriptFile, libDir)
  }
}
