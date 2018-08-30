package nunjucks.s2v8

import com.eclipsesource.v8.V8Object
import play.api.libs.json.Json

case class JavascriptError(obj: V8Object) extends RuntimeException {

  override val getMessage: String = {
    val message = obj.getString("message")
//    val stack = obj.getString("stack")
    obj.release()
    Json.stringify(Json.obj(
      "message" -> message
//      "stack" -> stack
    ))
  }
}

case class RouteHelperError(attemptedRoute: String, args: Seq[Any], wrapped: Throwable) extends RuntimeException {

  override lazy val getMessage: String =
    s"route not found: `$attemptedRoute`, with args: ${args.mkString(", ")}"

  override def getCause: Throwable = wrapped
}