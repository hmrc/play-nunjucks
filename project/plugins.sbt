resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("hmrc", "releases")
resolvers += Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns
)

(managedSources in Compile) += (baseDirectory.value / "project" / "PlayCrossCompilation.scala")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % PlayCrossCompilation.version)

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "2.0.0")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "2.13.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "1.13.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-play-cross-compilation" % "2.0.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
