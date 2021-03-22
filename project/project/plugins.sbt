resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("hmrc", "releases")
resolvers += Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns
)

addSbtPlugin("uk.gov.hmrc" % "sbt-play-cross-compilation" % "2.0.0")
