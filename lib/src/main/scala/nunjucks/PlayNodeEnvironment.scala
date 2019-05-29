package nunjucks

import java.util

import io.apigee.trireme.core.internal.RootModuleRegistry
import io.apigee.trireme.core.spi.NodeImplementation
import io.apigee.trireme.core.{InternalNodeModule, NativeNodeModule, NodeEnvironment, NodeModule}
import org.mozilla.javascript.{Context, Script}

import scala.collection.mutable
import scala.collection.JavaConverters._

class PlayNodeEnvironment(customModules: Map[String, NodeModule]) extends NodeEnvironment {

  override def getRegistry(version: String): RootModuleRegistry =
    new PlayModuleRegistry(super.getRegistry(version), customModules)
}

class PlayModuleRegistry(registry: RootModuleRegistry, customModules: Map[String, NodeModule]) extends RootModuleRegistry(registry.getImplementation) {

  private val modules: mutable.Map[String, NodeModule] =
    mutable.Map[String, NodeModule](customModules.toSeq: _*)

  private val internalModules: mutable.Map[String, InternalNodeModule] = mutable.Map.empty
  private val compiledModules: mutable.Map[String, Script] = mutable.Map.empty
  private val nativeModules: mutable.Map[String, NativeNodeModule] = mutable.Map.empty


  override def getImplementation: NodeImplementation =
    registry.getImplementation

  override def loadRoot(cx: Context): Unit =
    registry.loadRoot(cx)

  override def get(name: String): NodeModule =
    modules.getOrElse(name, registry.get(name))

  override def getInternal(name: String): NodeModule =
    internalModules.getOrElse(name, registry.getInternal(name))

  override def getNative(name: String): NodeModule =
    nativeModules.getOrElse(name, registry.getNative(name))

  override def getCompiledModule(name: String): Script =
    compiledModules.getOrElse(name, registry.getCompiledModule(name))

  override def getCompiledModuleNames: util.Set[String] =
    (registry.getCompiledModuleNames.asScala ++ compiledModules.keys).asJava

  override def getMainScript: Script =
    registry.getMainScript

  override protected def putCompiledModule(name: String, script: Script): Unit =
    compiledModules.put(name, script)

  override protected def putInternalModule(name: String, mod: InternalNodeModule): Unit =
    internalModules.put(name, mod)

  override protected def putNativeModule(name: String, mod: NativeNodeModule): Unit =
    nativeModules.put(name, mod)

  override protected def putRegularModule(name: String, mod: NodeModule): Unit =
    modules.put(name, mod)
}