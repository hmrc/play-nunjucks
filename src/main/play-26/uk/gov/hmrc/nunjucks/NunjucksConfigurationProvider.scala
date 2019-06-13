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
      .getOptional[Seq[String]]("nunjucks.viewPaths")
      .getOrElse(Seq.empty)

    val libPaths = ("" :: configuration
      .getOptional[Seq[String]]("nunjucks.libPaths")
      .getOrElse(Nil).toList)
      .map {
        dir =>
          (setup.libDir / dir).pathAsString
      }

    NunjucksConfiguration(viewPaths = viewPaths, libPaths = libPaths)
  }
}