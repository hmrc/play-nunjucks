resolvers += Resolver.jcenterRepo
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)
(managedSources in Compile) += (baseDirectory.value / "project" / "PlayCrossCompilation.scala")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % PlayCrossCompilation.version)

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "2.0.0")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.0.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-play-cross-compilation" % "2.2.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
