resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("hmrc", "releases")
resolvers += Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")
addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.1.9")
addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "2.0.0")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.11")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.13.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.15.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.13.0")
