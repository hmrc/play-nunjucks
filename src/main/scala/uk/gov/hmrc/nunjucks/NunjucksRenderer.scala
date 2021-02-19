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


import java.nio.file.Files
import java.util.concurrent.{ExecutorService, Executors}

import better.files.File
import io.apigee.trireme.core.{NodeModule, Sandbox}
import javax.inject.{Inject, Singleton}
import org.mozilla.javascript.json.JsonParser
import org.mozilla.javascript.{Context, JavaScriptException, Function => JFunction}
import org.webjars.WebJarExtractor
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.{Environment, Mode, PlayException}
import play.twirl.api.Html
import views.html.defaultpages.devError

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

@Singleton
class NunjucksRenderer @Inject() (
                                   setup: NunjucksSetup,
                                   configuration: NunjucksConfiguration,
                                   environment: Environment,
                                   reverseRoutes: NunjucksRoutesHelper,
                                   messagesApi: MessagesApi
                                 ) {

  private val threadPool: ExecutorService = {
    Executors.newFixedThreadPool(configuration.threadCount)
  }

  private val executionContext: ExecutionContext =
    ExecutionContext.fromExecutorService(threadPool)

  private val render = {

    val customModules: Map[String, NodeModule] = Map(
      NunjucksBootstrapModule.moduleName -> new NunjucksBootstrapModule(),
      NunjucksLoaderModule.moduleName    -> new NunjucksLoaderModule(),
      NunjucksHelperModule.moduleName    -> new NunjucksHelperModule()
    )

    val sandbox: Sandbox = new Sandbox()
    sandbox.setAsyncThreadPool(threadPool)

    val env = new PlayNodeEnvironment(customModules)
    env.setSandbox(sandbox)
    env.setDefaultNodeVersion("0.12")

    val script = env.createScript("[eval]", setup.script.toJava, configuration.libPaths.toArray)

    script.execute().getModuleResult().asInstanceOf[JFunction]
  }

  private val scope = {

    val context = Context.enter()
    val scope = context.initSafeStandardObjects(null, true)
    scope.sealObject()
    Context.exit()

    scope
  }

  def render(template: String, ctx: JsObject)(implicit request: RequestHeader): Future[Html] = {

    Future {

      val context = Context.enter()

      context.putThreadLocal("configuration", configuration)
      context.putThreadLocal("environment", environment)
      context.putThreadLocal("messagesApi", messagesApi)
      context.putThreadLocal("reverseRoutes", reverseRoutes)
      context.putThreadLocal("request", request)

      val result = Try {
        val obj = new JsonParser(context, scope).parseValue(Json.stringify(ctx))
        render.call(context, scope, null, Array(template, obj)).asInstanceOf[String]
      }.transform(Success.apply, toPlayException)

      context.removeThreadLocal("configuration")
      context.removeThreadLocal("environment")
      context.removeThreadLocal("messagesApi")
      context.removeThreadLocal("reverseRoutes")
      context.removeThreadLocal("request")

      Context.exit()

      result match {
        case Success(v) => Html(v)
        case Failure(e) =>
          lazy val runningLocally: Boolean = environment.mode.equals(Mode.Dev)
          if (runningLocally) {
            Logger.error(s"An error was encountered while trying to render Nunjucks template: $template.", e)
            visualisePlayException(e)
          }
          else throw e
      }
    }(executionContext)
  }

  def render(template: String)(implicit request: RequestHeader): Future[Html] =
    render(template, Json.obj())

  def render[A](template: String, ctx: A)(implicit request: RequestHeader, writes: OWrites[A]): Future[Html] =
    render(template, writes.writes(ctx))

  private val TemplateErrorWithLocation =
    """(.*): \((.*)\) \[Line (\d+), Column (\d+)\]$""".r
  private val TemplateError =
    """(.*): \((.*)\)$""".r

  private def visualisePlayException(e: Throwable)(implicit request: RequestHeader): Html = e match {
    case playException: PlayException => devError(
      playEditor = None,
      error = playException
    )
    case _ => throw e
  }

  private def toPlayException[A](e: Throwable): Failure[A] = {
    Failure {
      e match {
        case e: JavaScriptException =>

          def getSource(file: String): String =
            configuration.viewPaths
            .flatMap(path => environment.resourceAsStream(s"$path/$file"))
            .headOption
            .map(Source.fromInputStream)
            .map(_.mkString)
            .getOrElse("")

          val (first, stack) = e.details.splitAt(e.details.indexOf("\n"))

          first match {
            case TemplateErrorWithLocation(title, file, lpos, cpos) =>
              new PlayException.ExceptionSource(title, stack.trim, e) {
                override def line(): Integer = lpos.toInt
                override def position(): Integer = cpos.toInt
                override def input(): String = getSource(file)
                override def sourceName(): String = file
              }
            case TemplateError(_, _) =>
              new PlayException(first, stack.trim, e)
            case _ =>
              new RuntimeException(e.details, e)
          }
        case e => e
      }
    }
  }
}

@Singleton
class NunjucksSetup @Inject() (
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
