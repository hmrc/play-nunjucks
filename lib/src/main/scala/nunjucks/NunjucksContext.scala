package nunjucks

import java.net.URL
import java.nio.file.{Files, Paths}

import better.files._
import javax.inject.{Inject, Singleton}
import org.webjars.WebJarExtractor
import play.api.{Configuration, Environment, Logger, Mode}

import scala.concurrent.duration._

@Singleton
class DefaultNunjucksContext @Inject() (
                                         environment: Environment,
                                         configuration: Configuration
                                       ) extends NunjucksContext {

  private val logger = Logger(this.getClass)

  override val timeout: FiniteDuration =
    configuration.underlying.getInt("nunjucks.timeout").millis

  override val workingDirectory: File = {

    val configuredDirectory = configuration
      .getOptional[String]("nunjucks.workingDirectory")
      .map(toAbsoluteFile)

    configuredDirectory.foreach {
      file =>
        logger.info(s"setting working directory to: $file")
    }

    configuredDirectory.getOrElse {

      if (environment.mode == Mode.Dev || environment.mode == Mode.Test) {

        logger.info(s"no working directory set, in ${environment.mode}, " +
          s"creating temporary directory in `${environment.rootPath}/target`")

        toAbsoluteFile {
          "target/" + configuration.underlying
            .getString("nunjucks.devDirectory")
        }
      } else {

        logger.info("no working directory set, creating temporary directory")
        File.newTemporaryDirectory("nunjucks").deleteOnExit()
      }
    }.createIfNotExists(asDirectory = true, createParents = true)
  }


  override val libDirectory: File = {
    workingDirectory / configuration.underlying.getString("nunjucks.libDirectoryName")
  }

  override val nodeModulesDirectory: File = {

    val NODE_MODULES_ZIP = "nodeModules.zip"

    val nodeModulesDirectory = super.nodeModulesDirectory
    val nodeModulesZip = workingDirectory / NODE_MODULES_ZIP

    if (!nodeModulesDirectory.exists) {

      if (!nodeModulesZip.exists) {
        logger.info(s"extracting $NODE_MODULES_ZIP from jar")
        resourceToFile(NODE_MODULES_ZIP, nodeModulesZip)
      }

      logger.info(s"inflating $NODE_MODULES_ZIP")
      nodeModulesZip.unzipTo(workingDirectory)

      if (environment.mode == Mode.Prod) {
        logger.info(s"removing $NODE_MODULES_ZIP")
        nodeModulesZip.delete()
      }
    }

    nodeModulesDirectory
  }

  override val viewsDirectories: List[File] = {

    configuration
      .getOptional[Seq[String]]("nunjucks.viewPaths")
      .map {
        _.toList
          .flatMap(environment.resource)
          .map(toAbsoluteFile)
      }
      .getOrElse(List.empty)
      .ensuring(_.nonEmpty, "At least one view directory must be set at `nunjucks.viewPaths`")
  }

  override val libs: List[File] = {

    val extractor = new WebJarExtractor(environment.classLoader)

    configuration
      .getOptional[Seq[String]]("nunjucks.libs")
      .map {
        _.toList.map {
          lib =>

            val libDir = libDirectory / lib

            extractor.extractAllWebJarsTo(libDirectory.toJava)

            libDir
        }.toList
      }.getOrElse(List.empty)
  }

  private def toAbsoluteFile(url: URL): File =
    toAbsoluteFile(url.getFile)

  private def toAbsoluteFile(path: String): File = {
    if (Paths.get(path).isAbsolute) {
      path.toFile
    } else {
      environment.rootPath.toScala / path
    }
  }

  private def resourceToFile(path: String, file: File): File = {
    val resource = environment.resourceAsStream(path).get
    Files.copy(resource, file.path)
    file
  }
}

trait NunjucksContext {

  def workingDirectory: File

  def viewsDirectories: List[File]

  def libDirectory: File

  def libs: List[File]

  def nodeModulesDirectory: File =
    workingDirectory / "node_modules"

  def timeout: FiniteDuration
}
