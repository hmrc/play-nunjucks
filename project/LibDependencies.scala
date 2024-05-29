import sbt._

object LibDependencies {

  val play30Version = "3.0.3"

  private val shared = Seq(
    "com.github.pathikrit" %% "better-files"         % "3.9.1",
    "io.apigee.trireme"     % "trireme-core"         % "0.9.4",
    "io.apigee.trireme"     % "trireme-kernel"       % "0.9.4",
    "io.apigee.trireme"     % "trireme-node12src"    % "0.9.4",
    "org.webjars"           % "webjars-locator-core" % "0.58"
  )

  val play30 = Seq(
    "org.playframework"      %% "play"                 % play30Version,
    "org.playframework"      %% "play-filters-helpers" % play30Version,
    "org.playframework"      %% "play-test"            % play30Version % Test,
    "org.scalactic"          %% "scalactic"            % "3.2.3"       % Test,
    "org.scalatest"          %% "scalatest"            % "3.2.3"       % Test,
    "org.scalacheck"         %% "scalacheck"           % "1.14.0"      % Test,
    "org.scalamock"          %% "scalamock"            % "4.3.0"       % Test,
    "com.vladsch.flexmark"    % "flexmark-all"         % "0.64.8"      % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"   % "7.0.0"       % Test
  ) ++ shared

  private val itServerShared = Seq(
    "org.webjars.npm" % "govuk-frontend" % "3.3.0",
    "org.scalacheck" %% "scalacheck"     % "1.14.0" % "test",
    "org.scalactic"  %% "scalactic"      % "3.2.3"  % "test",
    "org.scalatest"  %% "scalatest"      % "3.2.3"  % "test"
  )

  val itServerPlay30 = Seq(
    "org.playframework"      %% "play-guice"         % "3.0.0",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.64.8" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0"  % "test"
  ) ++ itServerShared
}
