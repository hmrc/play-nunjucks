import org.apache.commons.lang3.SystemUtils
import play.core.PlayVersion

lazy val j2v8Version = "4.6.0"

lazy val commonSettings = Seq(
  organization := "uk.gov.hmrc",
  scalaVersion := "2.11.12"
)

lazy val libSettings = Seq(
  name := "play-nunjucks",
  version := "0.1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current % "test, provided",
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
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_macosx_x86_64" % j2v8Version
    )
  )

lazy val libLinux = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(libSettings)
  .settings(
    name := name.value + "-linux",
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_linux_x86_64" % j2v8Version
    )
  )

lazy val libWindows = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(libSettings)
  .settings(
    name := name.value + "-win32",
    libraryDependencies ++= Seq(
      "com.eclipsesource.j2v8" % "j2v8_win32_x86_64" % j2v8Version
    )
  )

lazy val lib = {
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
    libraryDependencies ++= Seq(
      "org.webjars.npm" % "govuk-frontend" % "1.0.0",
      "org.scalactic" %% "scalactic" % "3.0.5" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
    ),
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("lib/govuk-frontend/all.js"))
    ),
    pipelineStages in Assets := Seq(concat, uglify),
    WebKeys.webModuleGenerators in Assets += Def.task {

      val nodeModules = baseDirectory.value / "node_modules"
      val libs = (nodeModules ** "*" --- nodeModules) pair relativeTo(nodeModules)

      val mappings = libs.map {
        case (file, path) =>
          file -> (WebKeys.webJarsDirectory in Assets).value / "lib" / path
      }

      IO.copy(mappings)
      mappings.map(_._2)
    }.dependsOn(JsEngineKeys.npmNodeModules in Assets).taskValue,
    managedClasspath in Runtime += Def.task {

      val nodeModules = baseDirectory.value / "node_modules"
      val libs = (nodeModules ** "*" --- nodeModules) pair relativeTo(nodeModules)

      val Pattern = """^([^/]+)/(.*)$""".r
      val mappings = libs.filter(_._2.matches(Pattern.toString)).map {
        case (file, Pattern(lib, path)) =>
          file -> s"META-INF/resources/webjars/$lib/999-SNAPSHOT/$path"
      }

      val webJars = (resourceManaged in Compile).value / "webjars.jar"
      IO.zip(mappings, webJars)

      webJars
    }.dependsOn(JsEngineKeys.npmNodeModules in Assets).value
  )
