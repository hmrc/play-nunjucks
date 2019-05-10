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

  private val logger = Logger(getClass)

  val routes: Seq[JavaScriptReverseRoute] = {

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
              logger.info(s"route: $method")
              method.invoke(instance).asInstanceOf[JavaScriptReverseRoute]
          }
      }
  }
}

class DevelopmentNunjucksRoutesHelper @Inject() (environment: Environment) extends NunjucksRoutesHelper {

  private val logger = Logger(getClass)

  private class RoutesClassLoader(parent: ClassLoader, url: URL*) extends URLClassLoader(url.toArray, parent) {
    def packages: Array[Package] = getPackages
  }

  override def routes: Seq[JavaScriptReverseRoute] = {

    val rootPath = environment.rootPath.toScala.glob("target/*/routes").toList

    rootPath.foreach(path => logger.info(path.toString))

//    val classLoader = new RoutesClassLoader(environment.classLoader, ???)

    Seq.empty
  }
}