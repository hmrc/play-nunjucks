import play.core.PlayVersion

lazy val majorVersionNumber = 0

lazy val root = (project in file("."))
  .disablePlugins(SbtAutoBuildPlugin, SbtArtifactory)
  .settings(
    majorVersion := majorVersionNumber,
    parallelExecution in Test := false,
    test := {
      (test in(lib.project, Test)).value
      (test in(itServer.project, Test)).value
    },
    run := {
      (run in(itServer.project, Compile)).evaluated
    },
    publishAndDistribute := {
      (publishAndDistribute in lib.project).value
    }
  )

lazy val lib = (project in file("lib"))
  .enablePlugins(SbtWeb, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(
    scalaVersion := "2.11.12",
    name := "play-nunjucks-spike",
    organization := "uk.gov.hmrc",
    majorVersion := majorVersionNumber,
    makePublicallyAvailableOnBintray := false,
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-deprecation"
    ),
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % PlayVersion.current % "test, provided",
      "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
      "com.typesafe.play" %% "filters-helpers" % PlayVersion.current % "test, provided",
      "com.github.pathikrit" %% "better-files" % "3.5.0",
      "org.scalactic" %% "scalactic" % "3.0.7" % "test",
      "org.scalatest" %% "scalatest" % "3.0.7" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.scalamock" %% "scalamock" % "4.1.0" % "test",
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

lazy val itServer = (project in file("it-server"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(lib)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(
    name := "it-server",
    majorVersion := majorVersionNumber,
    organization := "uk.gov.hmrc",
    scalaVersion := "2.11.12",
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-deprecation"
    ),
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases"),
      Resolver.jcenterRepo
    ),
    libraryDependencies ++= Seq(
      guice,
      "org.webjars.npm" % "govuk-frontend" % "1.0.0",
      "org.scalactic" %% "scalactic" % "3.0.7" % "test",
      "org.scalatest" %% "scalatest" % "3.0.7" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "test"
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
