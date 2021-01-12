import sbt._
import Keys._
import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion.{Play25, Play26}

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = Play26) {

  lazy val version: String = playVersion match {
    case Play25 => throw new IllegalArgumentException("This library does not support Play 2.5")
    case Play26 => "2.6.20"
  }

  private lazy val playDir = "play-26"

  lazy val itServerCrossCompilationSettings: Seq[Def.Setting[_]] = Seq(
    crossScalaVersions ~= playCrossScalaBuilds,
    (unmanagedSourceDirectories in Compile) += (baseDirectory.value / s"app-$playDir"),
    (unmanagedResourceDirectories in Compile) += (baseDirectory.value / s"conf-$playDir"),
    (unmanagedSourceDirectories in Test) += (baseDirectory.value / s"test-$playDir"),
    (unmanagedResourceDirectories in Test) += (baseDirectory.value / "test" / s"resources-$playDir")
  )
}
