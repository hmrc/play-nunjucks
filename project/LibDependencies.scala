import play.sbt.PlayImport.filters
import sbt._

object LibDependencies {

  val play28Version = "2.8.20"
  val play29Version = "2.9.0"
  val play30Version = "3.0.0"

  private val shared = Seq(
    "com.github.pathikrit" %% "better-files"         % "3.9.1",
    "io.apigee.trireme"     % "trireme-core"         % "0.9.4",
    "io.apigee.trireme"     % "trireme-kernel"       % "0.9.4",
    "io.apigee.trireme"     % "trireme-node12src"    % "0.9.4",
    "org.webjars"           % "webjars-locator-core" % "0.35"
  )

  val play28 = Seq(
    "com.typesafe.play"      %% "play"               % play28Version,
    "com.typesafe.play"      %% "filters-helpers"    % play28Version,
    "com.typesafe.play"      %% "play-test"          % play28Version % Test,
    "org.scalactic"          %% "scalactic"          % "3.2.3"       % Test,
    "org.scalatest"          %% "scalatest"          % "3.2.3"       % Test,
    "org.scalacheck"         %% "scalacheck"         % "1.14.0"      % Test,
    "org.scalamock"          %% "scalamock"          % "4.3.0"       % Test,
    "org.pegdown"             % "pegdown"            % "1.6.0"       % Test,
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.35.10"     % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"       % Test
  ) ++ shared

  val play29 = Seq(
    "com.typesafe.play"      %% "play"               % play29Version,
    "com.typesafe.play"      %% "filters-helpers"    % s"$play29Version-M6",
    "com.typesafe.play"      %% "play-test"          % play29Version % Test,
    "org.scalactic"          %% "scalactic"          % "3.2.3"       % Test,
    "org.scalatest"          %% "scalatest"          % "3.2.3"       % Test,
    "org.scalacheck"         %% "scalacheck"         % "1.14.0"      % Test,
    "org.scalamock"          %% "scalamock"          % "4.3.0"       % Test,
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.64.8"      % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0"       % Test
  ) ++ shared

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

  val itServerPlay28 = Seq(
    filters,
    "org.webjars.npm"         % "govuk-frontend"     % "3.3.0",
    "com.typesafe.play"      %% "play-guice"         % "2.8.20",
    "org.scalactic"          %% "scalactic"          % "3.2.3"   % "test",
    "org.scalatest"          %% "scalatest"          % "3.2.3"   % "test",
    "org.scalacheck"         %% "scalacheck"         % "1.14.0"  % "test",
    "org.pegdown"             % "pegdown"            % "1.6.0"   % "test",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.35.10" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % "test"
  )

  val itServerPlay29 = Seq(
    filters,
    "com.typesafe.play"      %% "play-guice"         % "2.9.0",
    "org.webjars.npm"         % "govuk-frontend"     % "3.3.0",
    "org.scalactic"          %% "scalactic"          % "3.2.3"  % "test",
    "org.scalatest"          %% "scalatest"          % "3.2.3"  % "test",
    "org.scalacheck"         %% "scalacheck"         % "1.14.0" % "test",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.64.8" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0"  % "test"
  )

  val itServerPlay30 = Seq(
    filters,
    "org.webjars.npm"         % "govuk-frontend"     % "3.3.0",
    "org.playframework"      %% "play-guice"         % "3.0.0",
    "org.scalactic"          %% "scalactic"          % "3.2.3"  % "test",
    "org.scalatest"          %% "scalatest"          % "3.2.3"  % "test",
    "org.scalacheck"         %% "scalacheck"         % "1.14.0" % "test",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.64.8" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0"  % "test"
  )
}
