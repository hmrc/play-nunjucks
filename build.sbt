import org.apache.commons.lang3.SystemUtils
import play.core.PlayVersion

lazy val libraryVersion = "0.1.0-SNAPSHOT"

lazy val j2v8Version = "4.6.0"

lazy val root = (project in file("."))
  .settings(
    parallelExecution in Test := false,
    test := {
      (test in (lib.project, Test)).value
      (test in (itServer.project, Test)).value
    },
    publish := {
      (publish in plugin.project).value
      (publish in libMacOS.project).value
      (publish in libLinux.project).value
      (publish in libWindows.project).value
    },
    publishLocal := {
      (publishLocal in plugin.project).value
      (publishLocal in libMacOS.project).value
      (publishLocal in libLinux.project).value
      (publishLocal in libWindows.project).value
    },
    publishM2 := {
      (publishM2 in plugin.project).value
      (publishM2 in libMacOS.project).value
      (publishM2 in libLinux.project).value
      (publishM2 in libWindows.project).value
    },
    run := {
      (run in (itServer.project, Compile)).evaluated
    }
  )

lazy val commonSettings = Seq(
  organization := "uk.gov.hmrc",
  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation"
  )
)

lazy val libSettings = Seq(
  name := "play-nunjucks",
  version := libraryVersion,
  scalaVersion := "2.11.12",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current % "test, provided",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "com.typesafe.play" %% "filters-helpers" % PlayVersion.current % "test, provided",
    "com.github.pathikrit" %% "better-files" % "3.5.0",
    "org.scalactic" %% "scalactic" % "3.0.5" % "test",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
    "org.scalamock" %% "scalamock" % "4.1.0" % "test",
    "org.webjars" % "webjars-locator-core" % "0.35"
  ),
  resourceGenerators in Compile += Def.task {
    val nodeModules = (JsEngineKeys.npmNodeModules in Assets).value
    val filesToZip = nodeModules pair relativeTo(baseDirectory.value)
    val zipFile = (resourceManaged in Compile).value / "nodeModules.zip"
    IO.zip(filesToZip, zipFile)
    Seq(zipFile)
  }.taskValue,
  target := target.value / name.value
) ++ commonSettings

lazy val libMacOS = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(libSettings)
  .settings(
    name := name.value + "-mac",
    version := libraryVersion,
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_macosx_x86_64" % j2v8Version
    )
  )

lazy val libLinux = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(libSettings)
  .settings(
    name := name.value + "-linux",
    version := libraryVersion,
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_linux_x86_64" % j2v8Version
    )
  )

lazy val libWindows = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(libSettings)
  .settings(
    name := name.value + "-win32",
    version := libraryVersion,
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_win32_x86_64" % j2v8Version
    )
  )

def lib = {
  if (SystemUtils.IS_OS_LINUX) {
    libLinux
  } else if (SystemUtils.IS_OS_MAC_OSX) {
    libMacOS
  } else if (SystemUtils.IS_OS_WINDOWS) {
    libWindows
  } else {
    throw new IllegalStateException(s"Incompatible OS: ${System.getProperty("os.name")}")
  }
}

lazy val itServer = (project in file("it-server"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(lib)
  .settings(commonSettings)
  .settings(
    name := "it-server",
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      guice,
      "org.webjars.npm" % "govuk-frontend" % "1.0.0",
      "org.scalactic" %% "scalactic" % "3.0.5" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "test"
    ),
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("lib/govuk-frontend/all.js"))
    ),
    pipelineStages in Assets := Seq(concat, uglify)
  )

lazy val plugin = (project in file("plugin"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    sbtPlugin := true,
    version := libraryVersion,
    name := "play-nunjucks-plugin",
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.7",
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "uk.gov.hmrc.nunjucks.plugin"
  )
  .settings(commonSettings)
