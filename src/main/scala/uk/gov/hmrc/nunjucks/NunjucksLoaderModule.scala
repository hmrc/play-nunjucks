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

import io.apigee.trireme.core.{NodeModule, NodeRuntime}
import org.mozilla.javascript.annotations.JSFunction
import org.mozilla.javascript.{Context, Scriptable, ScriptableObject}
import play.api.Environment

import scala.io.Source

class NunjucksLoaderModule extends NodeModule {

  // $COVERAGE-OFF$
  override def getModuleName: String = NunjucksLoaderModule.moduleName
  // $COVERAGE-ON$

  override def registerExports(cx: Context, global: Scriptable, runtime: NodeRuntime): Scriptable = {
    ScriptableObject.defineClass(global, classOf[NunjucksLoader])
    cx.newObject(global, NunjucksLoader.className).asInstanceOf[NunjucksLoader]
  }
}

object NunjucksLoaderModule {

  val moduleName: String = "nunjucks-scala-loader"
}

class NunjucksLoader extends ScriptableObject {

  private def viewPaths: Seq[String] = {

    val context = Context.getCurrentContext

    val configuration =
      context
        .getThreadLocal("configuration")
        .asInstanceOf[NunjucksConfiguration]

    configuration.viewPaths
  }

  private def noCache: Boolean = {
    val context = Context.getCurrentContext

    val configuration =
      context
        .getThreadLocal("configuration")
        .asInstanceOf[NunjucksConfiguration]

    configuration.noCache
  }

  override def getClassName: String = NunjucksLoader.className

  @JSFunction
  def getSource(view: String): Scriptable = {

    val context = Context.getCurrentContext

    val environment =
      context
        .getThreadLocal("environment")
        .asInstanceOf[Environment]

    viewPaths
      .flatMap(path => environment.resourceAsStream(s"$path/$view"))
      .headOption
      .map(Source.fromInputStream)
      .map(_.mkString)
      .map { content =>
        val obj = context.newObject(getParentScope)
        obj.put("path", obj, view)
        obj.put("src", obj, content)
        obj.put("noCache", obj, noCache)

        obj
      }
      .orNull
  }
}

object NunjucksLoader {

  val className = "_nunjucksScalaLoader"
}
