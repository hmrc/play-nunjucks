import sbt._
import Keys._
import uk.gov.hmrc.playcrosscompilation.AbstractPlayCrossCompilation
import uk.gov.hmrc.playcrosscompilation.PlayVersion.Play26

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = Play26) {

  lazy val version: String = playVersion match {
    case Play26 => "2.6.20"
  }

  private lazy val playDirSuffix = "play-26"

  lazy val itServerCrossCompilationSettings: Seq[Def.Setting[_]] = Seq(
    crossScalaVersions ~= playCrossScalaBuilds,
    (unmanagedSourceDirectories in Compile) += (baseDirectory.value / s"app-$playDirSuffix"),
    (unmanagedResourceDirectories in Compile) += (baseDirectory.value / s"conf-$playDirSuffix"),
    (unmanagedSourceDirectories in Test) += (baseDirectory.value / s"test-$playDirSuffix"),
    (unmanagedResourceDirectories in Test) += (baseDirectory.value / "test" / s"resources-$playDirSuffix")
  )
}
