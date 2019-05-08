package nunjucks.s2v8

import com.eclipsesource.v8.V8Object
import play.api.PlayException
import better.files._

object JavascriptError {

  private final case class ExceptionInfo(file: File, description: String, lineNumber: Option[Int] = None)

  def apply(obj: V8Object): RuntimeException = {

    val message = obj.getString("message")
    val stack = obj.getString("stack")
    obj.release()

    val exceptionInfo: Option[ExceptionInfo] = {

      if (message.indexOf("\n") >= 0) {

        val (firstLine, description) = message.splitAt(message.indexOf("\n"))

        val ExceptionInfoWithLineNumber = """^\((.+)\) \[Line (\d+), Column (\d+)\]$""".r
        val ExceptionInfoWithoutLineNumber = """^\((.+)\)$""".r

        firstLine match {
          case ExceptionInfoWithLineNumber(file, line) =>
            Some(ExceptionInfo(file.toFile, description.replaceAll("\n", ""), Some(line.toInt)))
          case ExceptionInfoWithoutLineNumber(file) =>
            Some(ExceptionInfo(file.toFile, description.replaceAll("\n", "")))
          case _ =>
            None
        }
      } else {
        None
      }
    }

    exceptionInfo.map {
      exceptionInfo =>
        new PlayException.ExceptionSource("Nunjucks exception", exceptionInfo.description) {

          override def line(): Integer = new Integer(exceptionInfo.lineNumber.getOrElse(0))

          override def position(): Integer = 0

          override def input(): String = exceptionInfo.file.contentAsString

          override def sourceName(): String = exceptionInfo.file.pathAsString
        }
    }.getOrElse {
      new RuntimeException(message)
    }
  }
}
