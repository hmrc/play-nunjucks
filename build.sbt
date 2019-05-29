import play.core.PlayVersion

lazy val libraryVersion = "0.1.0-SNAPSHOT"

lazy val j2v8Version = "4.6.0"

lazy val root = (project in file("."))
  .settings(
    parallelExecution in Test := false,
    test := {
      (test in(lib.project, Test)).value
      (test in(itServer.project, Test)).value
    },
    run := {
      (run in(itServer.project, Compile)).evaluated
    }
  )

lazy val commonSettings = Seq(
  organization := "uk.gov.hmrc",
  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation"
  )
)

lazy val lib = (project in file("lib"))
  .enablePlugins(SbtWeb)
  .settings(
    version := libraryVersion,
    scalaVersion := "2.11.12",
    name := "play-uk.gov.hmrc.nunjucks",
    version := libraryVersion,
    scalaVersion := "2.11.12",
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
  .settings(commonSettings)
  .settings(
    name := "it-server",
    scalaVersion := "2.11.12",
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
