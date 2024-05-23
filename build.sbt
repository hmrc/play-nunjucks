import sbt.Path.relativeTo

val scala2_13 = "2.13.12"

ThisBuild / majorVersion := 1
ThisBuild / isPublicArtefact := true
ThisBuild / scalaVersion := scala2_13

lazy val playNunjucksPlay30 = Project("play-nunjucks-play-30", file("play-nunjucks"))
  .enablePlugins(SbtWeb)
  .settings(
    libraryDependencies ++= LibDependencies.play30,
    coverageExcludedPackages := "<empty>;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.nunjucks.PlayModuleRegistry",
    buildInfoKeys ++= Seq[BuildInfoKey]("playVersion" -> LibDependencies.play30Version),
    Compile / resourceGenerators += npmModulesTarballTask.taskValue,
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    JsEngineKeys.npmPreferSystemInstalledNpm := true,
    JsEngineKeys.npmSubcommand := JsEngineKeys.NpmSubcommand.Ci
  )

def npmModulesTarballTask = Def.task {
  val nodeModules = (Assets / JsEngineKeys.npmNodeModules).value
  val filesToZip  = nodeModules pair relativeTo(baseDirectory.value)
  val zipFile     = (Compile / resourceManaged).value / "nodeModules.tar"
  IO.zip(filesToZip, zipFile, None)
  Seq(zipFile)
}

lazy val itServerPlay30 = Project("it-server-play-30", file("it-server"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(playNunjucksPlay30)
  .settings(
    publish / skip := true,
    libraryDependencies ++= LibDependencies.itServerPlay30,
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("lib/govuk-frontend/govuk/all.js"))
    ),
    Assets / pipelineStages := Seq(concat, uglify),
    uglifyOps := UglifyOps.singleFile, // no source map
    coverageEnabled := false,
    Test / fork := true,
    Test / javaOptions ++= Seq(
      "-Dconfig.resource=test.application.conf"
    )
  )
