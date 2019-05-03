import org.apache.commons.lang3.SystemUtils
import sbt.{AutoPlugin, Def}
import sbt._
import Keys._
import uk.gov.hmrc.nunjucks.plugin.BuildInfo

object NunjucksPlugin extends AutoPlugin {

  val libSetting = if (SystemUtils.IS_OS_LINUX) {
    "uk.gov.hmrc" % "play-nunjucks-linux" % BuildInfo.version
  } else if (SystemUtils.IS_OS_MAC_OSX) {
    "uk.gov.hmrc" % "play-nunjucks-mac" % BuildInfo.version
  } else if (SystemUtils.IS_OS_WINDOWS) {
    "uk.gov.hmrc" % "play-nunjucks-win32" % BuildInfo.version
  } else {
    throw new IllegalStateException(s"Incompatible OS: ${System.getProperty("os.name")}")
  }

  override def buildSettings: Seq[Def.Setting[_]] =
    Seq(libraryDependencies += libSetting)
}
