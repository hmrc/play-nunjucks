package nunjucks

import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

class NunjucksModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[NunjucksContext].to[DefaultNunjucksContext]
    )
}
