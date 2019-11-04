package uk.gov.hmrc.nunjucks

import javax.inject.{Inject, Provider, Singleton}
import play.api.Configuration

@Singleton
class NunjucksConfigurationProvider @Inject() (
                                                configuration: Configuration,
                                                setup: NunjucksSetup
                                              ) extends Provider[NunjucksConfiguration] {

  override def get(): NunjucksConfiguration = {

    val viewPaths: Seq[String] = configuration
      .get[Seq[String]]("nunjucks.viewPaths")

    val libPaths = ("" :: configuration
      .getOptional[Seq[String]]("nunjucks.libPaths")
      .getOrElse(Nil).toList)
      .map {
        dir =>
          (setup.libDir / dir).pathAsString
      }

    val threadCount =
      configuration.get[Int]("nunjucks.threadCount")

    val noCache =
      configuration.get[Boolean]("nunjucks.noCache")

    NunjucksConfiguration(
      viewPaths   = viewPaths,
      libPaths    = libPaths,
      threadCount = threadCount,
      noCache = noCache
    )
  }
}
