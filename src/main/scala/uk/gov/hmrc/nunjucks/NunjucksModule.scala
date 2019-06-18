package uk.gov.hmrc.nunjucks

import play.api.{Configuration, Environment, Mode}
import play.api.inject.{Binding, Module}

class NunjucksModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[NunjucksRenderer].toSelf.eagerly,
      bind[NunjucksConfiguration].toProvider[NunjucksConfigurationProvider],
      bind[NunjucksSetup].toSelf.eagerly,
      if (environment.mode == Mode.Dev) {
        bind[NunjucksRoutesHelper].to[DevelopmentNunjucksRoutesHelper]
      } else {
        bind[NunjucksRoutesHelper].to[ProductionNunjucksRoutesHelper]
      }
    )
}
