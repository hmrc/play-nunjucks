package nunjucks

import play.api.{Configuration, Environment, Mode}
import play.api.inject.{Binding, Module}

class NunjucksModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[NunjucksContext].to[DefaultNunjucksContext],
      if (environment.mode == Mode.Prod) {
        bind[NunjucksRoutesHelper].to[ProductionNunjucksRoutesHelper]
      } else {
        bind[NunjucksRoutesHelper].to[DevelopmentNunjucksRoutesHelper]
      }
    )
}
