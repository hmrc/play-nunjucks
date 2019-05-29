package nunjucks

import java.net.{URL, URLClassLoader}

import better.files._
import javax.inject.{Inject, Singleton}
import play.api.{Environment, Logger}
import play.api.routing.JavaScriptReverseRoute

import scala.util.Try

trait NunjucksRoutesHelper {
  def routes: Seq[JavaScriptReverseRoute]
}

@Singleton
class ProductionNunjucksRoutesHelper @Inject() extends NunjucksRoutesHelper {

  lazy val routes: Seq[JavaScriptReverseRoute] = {

    Package.getPackages
      .map(_.getName)
      .flatMap(p => Try(Class.forName(s"$p.routes$$javascript").getDeclaredFields).toOption)
      .flatten
      .flatMap {
        field =>

          val instance = field.get(null)
          val fieldClass = field.getType

          fieldClass.getDeclaredMethods.filter {
            _.getReturnType == classOf[JavaScriptReverseRoute]
          }.map {
            method =>
              method.invoke(instance).asInstanceOf[JavaScriptReverseRoute]
          }
      }
  }
}

class DevelopmentNunjucksRoutesHelper @Inject() (environment: Environment) extends NunjucksRoutesHelper {

  override def routes: Seq[JavaScriptReverseRoute] = {

    val routesUrls = environment.rootPath.toScala.glob("target/*/classes").toList.filter(_.isDirectory).map(_.url)

    val classLoader = new URLClassLoader(routesUrls.toArray, environment.classLoader)

    environment.rootPath.toScala.glob("target/*/classes/**/routes.class")
      .toList
      .map(environment.rootPath.toScala.relativize)
      .map(path => path.toString.replaceAll("^target/[^/]+/classes/", "").replaceAll("routes.class$", "").replaceAll("/", "."))
      .flatMap(p => Try(Class.forName(s"${p}routes$$javascript", false, classLoader).getDeclaredFields).toOption)
      .flatten
      .flatMap {
        field =>

          val instance = field.get(null)
          val fieldClass = field.getType

          fieldClass.getDeclaredMethods.filter {
            _.getReturnType == classOf[JavaScriptReverseRoute]
          }.map {
            method =>
              method.invoke(instance).asInstanceOf[JavaScriptReverseRoute]
          }
      }
  }
}
