import play.core.PlayVersion

lazy val majorVersionNumber = 0

lazy val lib = (project in file("."))
  .enablePlugins(SbtWeb, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(commonSettings: _*)
  .settings(PlayCrossCompilation.playCrossCompilationSettings: _*)
  .settings(
    name := "play-nunjucks-spike",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % PlayVersion.current % "test, provided",
      "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
      "com.typesafe.play" %% "filters-helpers" % PlayVersion.current % "test, provided",
      "com.github.pathikrit" %% "better-files" % "3.5.0",
      "org.scalactic" %% "scalactic" % "3.0.7" % "test",
      "org.scalatest" %% "scalatest" % "3.0.7" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.scalamock" %% "scalamock" % "4.1.0" % "test",
      "org.pegdown" % "pegdown" % "1.6.0" % "test",
      "io.apigee.trireme" % "trireme-core" % "0.9.4",
      "io.apigee.trireme" % "trireme-kernel" % "0.9.4",
      "io.apigee.trireme" % "trireme-node12src" % "0.9.4",
      "org.webjars" % "webjars-locator-core" % "0.35"
    ),
    resourceGenerators in Compile += Def.task {
      val nodeModules = (JsEngineKeys.npmNodeModules in Assets).value
      val filesToZip = nodeModules pair relativeTo(baseDirectory.value)
      val zipFile = (resourceManaged in Compile).value / "nodeModules.tar"
      IO.zip(filesToZip, zipFile)
      Seq(zipFile)
    }.taskValue
  )

(test in(lib.project, Test)) := {
  (test in(lib.project, Test)).value
  (test in(itServer.project, Test)).value
}

lazy val itServer = (project in file("it-server"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(lib)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(commonSettings: _*)
  .settings(PlayCrossCompilation.itServerCrossCompilationSettings: _*)
  .settings(
    name := "it-server",
    libraryDependencies ++= PlayCrossCompilation.dependencies(
      shared = Seq(
        filters,
        "org.webjars.npm" % "govuk-frontend" % "1.0.0",
        "org.scalactic" %% "scalactic" % "3.0.7" % "test",
        "org.scalatest" %% "scalatest" % "3.0.7" % "test",
        "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
        "org.pegdown" % "pegdown" % "1.6.0" % "test",
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "test"
      ),
      play26 = Seq(
        "com.typesafe.play" %% "play-guice" % PlayVersion.current
      )
    ),
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("lib/govuk-frontend/all.js"))
    ),
    pipelineStages in Assets := Seq(concat, uglify)
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  ),
  unmanagedSourceDirectories ++= Seq(
    baseDirectory.value / "test-utils"
  )
)

lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  organization := "uk.gov.hmrc",
  majorVersion := majorVersionNumber,
  makePublicallyAvailableOnBintray := false,
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12"),
  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation"
  ),
  resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.bintrayRepo("hmrc", "snapshots"),
    Resolver.bintrayRepo("hmrc", "release-candidates"),
    Resolver.typesafeRepo("releases"),
    Resolver.jcenterRepo
  )
)
