package nunjucks.s2v8

import com.eclipsesource.v8.V8Object
import play.api.PlayException
import better.files._

object JavascriptError {

  def apply(obj: V8Object): PlayException.ExceptionSource = {

    val message = obj.getString("message")
    val stack = obj.getString("stack")
    obj.release()

    val exceptionInfo = {

      val (firstLine, description) = message.splitAt(message.indexOf("\n"))
      val ExceptionInfo = """^\((.+)\) \[Line (\d+), Column (\d+)\]$""".r
      val ExceptionInfo(file, line, column) = firstLine

      (file, line, column, description.replaceAll("\n", ""))
    }

    new PlayException.ExceptionSource("Nunjucks exception", exceptionInfo._4) {

      override def line(): Integer = exceptionInfo._2.toInt

      override def position(): Integer = exceptionInfo._3.toInt

      override def input(): String =
        exceptionInfo._1.toFile.contentAsString

      override def sourceName(): String = exceptionInfo._1
    }
  }
}
