package nunjucks

import com.eclipsesource.v8._
import nunjucks.s2v8.{SNodeJS, SV8Object}
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.{Call, RequestHeader}
import views.html.helper.CSRF

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal

class Nunjucks private(
                        delegate: V8Object,
                        njkContext: NunjucksContext,
                        classLoader: ClassLoader
                      )(implicit nodeJS: SNodeJS) extends SV8Object(delegate) {

  import s2v8._

  private implicit val timeout: FiniteDuration = 100.millis

  private def registerRoutesHelper(): Unit = {

    val fn = new V8Function(delegate.getRuntime, new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        val routeString = parameters.getString(0)

        val args = (1 until parameters.length)
          .map(parameters.get)
          .flatMap(Option(_))
          .toList

        try {

          val pieces = routeString.split("\\.")

          val `package` = pieces
            .dropRight(2)
            .mkString(".")

          val `class` = pieces.init.last
          val route   = pieces.last
          val field   = Class.forName(`package`, false, classLoader).getField(`class`)
          val method  = field.getType.getMethods.find(_.getName == route).get

          method.invoke(field.get(null), args: _*).asInstanceOf[Call].url
        } catch {
          case NonFatal(e) =>
            //throw RouteHelperError(routeString, args, e)
          throw e
        }
      }
    })

    addGlobal("route", fn)
  }

  private def registerMessagesHelper(messages: Messages): Unit = {

    val fn = new V8Function(delegate.getRuntime, new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        val key = parameters.getString(0)

        val args = (1 until parameters.length)
            .map(parameters.get)
            .flatMap(Option(_))
            .toList

        messages(key, args: _*)
      }
    })

    addGlobal("messages", fn)
  }

  private def registerCsrfHelper(request: RequestHeader): Unit = {

    val fn = new V8Function(delegate.getRuntime, new JavaCallback {
      override def invoke(receiver: V8Object, parameters: V8Array): AnyRef = {

        CSRF.formField(request).toString()
      }
    })

    addGlobal("csrf", fn)
  }

  private def addGlobal(key: String, value: V8Value): Unit = {

    val params = new V8Array(nodeJS.runtime)
        .push(key)
        .push(value)

    delegate.executeVoidFunction("addGlobal", params)
    params.release()
    value.release()
  }

  def render(view: String, context: JsObject, messages: Messages, request: RequestHeader)(implicit ec: ExecutionContext): Try[String] = {
    registerMessagesHelper(messages)
    registerCsrfHelper(request)
    executeStringFnViaCallback("render", view, context)(nodeJS, ec, njkContext.timeout)
  }

  override def release(): Unit = {
    super.release()
    nodeJS.release()
  }
}

object Nunjucks {

  def apply(context: NunjucksContext, classLoader: ClassLoader): Nunjucks = {

    val runtime = SNodeJS.create()

    val nunjucks =
      runtime.require(context.nodeModulesDirectory / "nunjucks")

    val viewDirectories = context.libDirectory :: context.viewsDirectories

    val environment =
      nunjucks.executeObjectFn(
        "configure",
        viewDirectories.map {
          p =>
            Json.toJson(p.pathAsString)
        })

    nunjucks.release()

    val instance = new Nunjucks(environment.delegate, context, classLoader)(runtime)
    instance.registerRoutesHelper()
    instance
  }
}
