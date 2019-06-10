package uk.gov.hmrc.nunjucks

import javax.inject.{Inject, Provider, Singleton}
import scala.collection.JavaConverters._
import play.api.Configuration

@Singleton
class NunjucksConfigurationProvider @Inject() (
                                                configuration: Configuration,
                                                setup: NunjucksSetup
                                              ) extends Provider[NunjucksConfiguration] {

  override def get(): NunjucksConfiguration = {

    val viewPaths: Seq[String] = configuration
      .getStringList("nunjucks.viewPaths")
      .map(_.asScala)
      .getOrElse(Seq.empty)

    val libPaths: Seq[String] = ("" :: configuration
        .getStringList("nunjucks.libPaths")
        .map(_.asScala)
        .getOrElse(Nil).toList)
        .map {
          dir =>
            (setup.libDir / dir).pathAsString
        }

    NunjucksConfiguration(viewPaths = viewPaths, libPaths = libPaths)
  }
}