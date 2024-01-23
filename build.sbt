import sbt.Path.relativeTo

ThisBuild / majorVersion := 1
ThisBuild / isPublicArtefact := true

val scala2_12 = "2.12.18"
val scala2_13 = "2.13.12"

def copySources(module: Project) = Seq(
  Compile / scalaSource := (module / Compile / scalaSource).value,
  Compile / resourceDirectory := (module / Compile / resourceDirectory).value,
  Test / scalaSource := (module / Test / scalaSource).value,
  Test / resourceDirectory := (module / Test / resourceDirectory).value
)

def copyPlayResources(module: Project) = Seq(
//  Compile / TwirlKeys.compileTemplates / sourceDirectories += (module / baseDirectory).value / s"src/main/twirl",
  Compile / routes / sources ++= {
    //baseDirectory.value / s"../src-common/main/resources/hmrcfrontend.routes"
    // compile any routes files in the root named "routes" or "*.routes"
    val dirs = (module / Compile / unmanagedResourceDirectories).value
    (dirs * "routes").get ++ (dirs * "*.routes").get
  }
)

lazy val library = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(
    sys.env.get("PLAY_VERSION") match {
      case Some("2.8") => playNunjucksPlay28
      case Some("2.9") => playNunjucksPlay29
      case _           => playNunjucksPlay30
    }
  )

lazy val playNunjucksPlay28 = Project("play-nunjucks-play-28", file("play-nunjucks-play-28"))
  .enablePlugins(SbtWeb)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(scalaVersion := scala2_12, crossScalaVersions := Seq(scala2_12, scala2_13))
  .settings(
    libraryDependencies ++= LibDependencies.play28,
    coverageExcludedPackages := "<empty>;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.nunjucks.PlayModuleRegistry",
    buildInfoKeys ++= Seq[BuildInfoKey]("playVersion" -> LibDependencies.play28Version)
  )
  .settings(Compile / resourceGenerators += npmModulesTarballTask.taskValue)

lazy val playNunjucksPlay29 = Project("play-nunjucks-play-29", file("play-nunjucks-play-29"))
  .enablePlugins(SbtWeb)
  .settings(copySources(playNunjucksPlay28))
  .settings(copyPlayResources(playNunjucksPlay28))
  .settings(inConfig(Test)(testSettings): _*)
  .settings(scalaVersion := scala2_13)
  .settings(
    libraryDependencies ++= LibDependencies.play29,
    coverageExcludedPackages := "<empty>;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.nunjucks.PlayModuleRegistry"
  )
  .settings(
    Compile / resourceGenerators += npmModulesTarballTask.taskValue
      .dependsOn(copyNpmFilesTask.taskValue)
  )

lazy val playNunjucksPlay30 = Project("play-nunjucks-play-30", file("play-nunjucks-play-30"))
  .enablePlugins(SbtWeb)
  .settings(copySources(playNunjucksPlay28))
  .settings(copyPlayResources(playNunjucksPlay28))
  .settings(inConfig(Test)(testSettings): _*)
  .settings(scalaVersion := scala2_13)
  .settings(
    libraryDependencies ++= LibDependencies.play30,
    coverageExcludedPackages := "<empty>;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.nunjucks.PlayModuleRegistry"
  )
  .settings(
    Compile / resourceGenerators += npmModulesTarballTask.taskValue
      .dependsOn(copyNpmFilesTask.taskValue)
  )

def copyNpmFilesTask = Def.task {
  IO.copy(
    Seq("package.json", "package-lock.json")
      .map(file => baseDirectory.value / ".." / "play-nunjucks-play-28" / file -> baseDirectory.value / file)
  )
}

def npmModulesTarballTask = Def.task {
  val nodeModules = (Assets / JsEngineKeys.npmNodeModules).value
  val filesToZip  = nodeModules pair relativeTo(baseDirectory.value)
  val zipFile     = (Compile / resourceManaged).value / "nodeModules.tar"
  IO.zip(filesToZip, zipFile, None)
  Seq(zipFile)
}

lazy val itServer = sys.env.get("PLAY_VERSION") match {
  case Some("2.8") => itServerPlay28
  case Some("2.9") => itServerPlay29
  case _           => itServerPlay30
}

lazy val itServerPlay28 = Project("it-server-play-28", file("it-server-play-28"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(playNunjucksPlay28)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(sharedItServerSettings: _*)
  .settings(
    scalaVersion := scala2_12,
    crossScalaVersions := Seq(scala2_12, scala2_13)
  )
  .settings(libraryDependencies ++= LibDependencies.itServerPlay28)

lazy val itServerPlay29 = Project("it-server-play-29", file("it-server-play-29"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(playNunjucksPlay29)
  .settings(copySources(itServerPlay28))
  .settings(inConfig(Test)(testSettings): _*)
  .settings(sharedItServerSettings: _*)
  .settings(scalaVersion := scala2_13)
  .settings(libraryDependencies ++= LibDependencies.itServerPlay29)

lazy val itServerPlay30 = Project("it-server-play-30", file("it-server-play-30"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(playNunjucksPlay30)
  .settings(copySources(itServerPlay28))
  .settings(inConfig(Test)(testSettings): _*)
  .settings(sharedItServerSettings: _*)
  .settings(scalaVersion := scala2_13)
  .settings(libraryDependencies ++= LibDependencies.itServerPlay30)

lazy val sharedItServerSettings = Seq(
  Concat.groups := Seq(
    "javascripts/application.js" -> group(Seq("lib/govuk-frontend/govuk/all.js"))
  ),
  Assets / pipelineStages := Seq(concat, uglify),
  coverageEnabled := false
)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)

library.project / Test / test := {
  (library.project / Test / test).value
  (itServer.project / Test / test).value
}
