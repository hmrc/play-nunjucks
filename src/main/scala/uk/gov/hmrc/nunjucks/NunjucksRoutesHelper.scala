/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nunjucks

import better.files._
import play.api.Environment
import play.api.routing.JavaScriptReverseRoute

import java.net.URLClassLoader
import javax.inject.{Inject, Singleton}
import scala.util.Try

trait NunjucksRoutesHelper {
  def routes: Seq[JavaScriptReverseRoute]
}

@Singleton
class ProductionNunjucksRoutesHelper @Inject() extends NunjucksRoutesHelper {

  lazy val routes: Seq[JavaScriptReverseRoute] =
    Package.getPackages
      .map(_.getName)
      .flatMap(p => Try(Class.forName(s"$p.routes$$javascript").getDeclaredFields).toOption)
      .flatten
      .flatMap { field =>
        val instance   = field.get(null)
        val fieldClass = field.getType

        fieldClass.getDeclaredMethods
          .filter {
            _.getReturnType == classOf[JavaScriptReverseRoute]
          }
          .map { method =>
            method.invoke(instance).asInstanceOf[JavaScriptReverseRoute]
          }
      }
}

class DevelopmentNunjucksRoutesHelper @Inject() (environment: Environment) extends NunjucksRoutesHelper {

  override def routes: Seq[JavaScriptReverseRoute] = {

    val routesUrls = environment.rootPath.toScala.glob("target/*/classes").toList.filter(_.isDirectory).map(_.url)

    val classLoader = new URLClassLoader(routesUrls.toArray, environment.classLoader)

    environment.rootPath.toScala
      .glob("target/*/classes/**/routes.class")
      .toList
      .map(environment.rootPath.toScala.relativize)
      .map(path =>
        path.toString.replaceAll("^target/[^/]+/classes/", "").replaceAll("routes.class$", "").replaceAll("/", ".")
      )
      .flatMap(p => Try(Class.forName(s"${p}routes$$javascript", false, classLoader).getDeclaredFields).toOption)
      .flatten
      .flatMap { field =>
        val instance   = field.get(null)
        val fieldClass = field.getType

        fieldClass.getDeclaredMethods
          .filter {
            _.getReturnType == classOf[JavaScriptReverseRoute]
          }
          .map { method =>
            method.invoke(instance).asInstanceOf[JavaScriptReverseRoute]
          }
      }
  }
}
