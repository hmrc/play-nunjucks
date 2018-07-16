package nunjucks

import java.nio.file.{Files, Paths}

import better.files._
import com.google.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Logger, Mode}

import scala.collection.JavaConverters._

@Singleton
class DefaultNunjucksContext @Inject() (
                                         environment: Environment,
                                         configuration: Configuration
                                       ) extends NunjucksContext {

  private val logger = Logger(this.getClass)

  override lazy val libDirectory: File = {
    workingDirectory / configuration.underlying.getString("nunjucks.libDirectoryName")
  }

  override lazy val nodeModulesDirectory: File = {

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

  override lazy val workingDirectory: File = {

    val configuredDirectory = configuration
      .getString("nunjucks.workingDirectory")
      .map(toAbsoluteFile)

    configuredDirectory.foreach {
      file =>
        logger.info(s"setting working directory to: $file")
    }

    configuredDirectory.getOrElse {

      if (environment.mode < Mode.Prod) {

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

  override lazy val viewsDirectories: List[String] = {

    configuration
      .getStringList("nunjucks.viewPaths")
      .map(_.asScala.toList)
      .getOrElse(List.empty)
      .ensuring(_.nonEmpty, "At least one view directory must be set at `nunjucks.viewPaths`")
  }

  override val libs: List[File] = {

    configuration
      .getStringList("nunjucks.libs")
      .map {
        a =>
        a.asScala.map {
          lib =>

            val jar = {
              val fullPath = environment.resource(s"META-INF/resources/webjars/$lib").get.getPath
              (fullPath.substring(5, fullPath.indexOf(".jar")) + ".jar").toFile
            }

            val tmp = workingDirectory / "tmp" / lib
            val resourceDir = tmp / "META-INF" / "resources" / "webjars" / lib
            val libDir = libDirectory / lib

            logger.info(s"unzipping $jar to $tmp")
            jar.unzipTo(tmp)

            logger.info(s"copying resources from $tmp to $libDir")
            resourceDir.glob("**/*.njk").foreach {
              file =>
                val path = resourceDir
                  .glob("*").toList.head
                  .relativize(file).toString
                val newFile = libDir / path
                logger.debug(s"copying $file to $newFile")
                newFile.createIfNotExists(createParents = true)
                file.copyTo(newFile, overwrite = true)
            }

            libDir
        }.toList
      }.getOrElse(List.empty)
  }

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

  def viewsDirectories: List[String]

  def libDirectory: File

  def libs: List[File]

  def nodeModulesDirectory: File =
    workingDirectory / "node_modules"
}
