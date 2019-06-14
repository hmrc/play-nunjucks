import sbt._
import Keys._
import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion.{Play25, Play26}

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = Play25) {

  lazy val version: String = playVersion match {
    case Play25 => "2.5.19"
    case Play26 => "2.6.20"
  }

  private lazy val playDir =
    if (playVersion == Play25) "play-25" else "play-26"

  lazy val itServerCrossCompilationSettings: Seq[Def.Setting[_]] = Seq(
    crossScalaVersions ~= playCrossScalaBuilds,
    (unmanagedSourceDirectories in Compile) += (baseDirectory.value / s"app-$playDir"),
    (unmanagedResourceDirectories in Compile) += (baseDirectory.value / s"conf-$playDir"),
    (unmanagedSourceDirectories in Test) += (baseDirectory.value / s"test-$playDir"),
    (unmanagedResourceDirectories in Test) += (baseDirectory.value / "test" / s"resources-$playDir")
  )

  lazy val rootCrossCompilationSettings: Seq[Def.Setting[_]] = Seq(
    crossScalaVersions ~= playCrossScalaBuilds
  )
}
