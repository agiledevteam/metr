package com.lge.metr

import java.io.File
import spoon.reflect.Factory
import spoon.support.DefaultCoreFactory
import spoon.support.StandardEnvironment
import spoon.support.builder.CtResource
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.declaration.CtExecutable
import java.io.PrintWriter

class SpoonLauncher(config: Config) extends CallCounter with LocCounter with CCCounter {
  val factory = SpoonLauncher.createFactory

  def loadAll() {
    ClasspathHolder.additionalClasspath = config.deps.mkString(File.pathSeparator)
    val builder = factory.getBuilder
    config.src foreach (builder.addInputSource(_))
    builder.build
  }

  def generate(reportFile: String) {
    val methods = allMethods

    val handlers: List[CtExecutable[_] => Any] = List(sloc(_), dloc(_), nameFor(_))
    val p = new PrintWriter(reportFile)
    methods foreach { m =>
      p.println(handlers.map(h => h(m)).mkString("\t"))
    }
    p.close
  }

  def allMethods =
    factory.all[CtExecutable[_]].filter(m => !m.isImplicit && m.getBody != null)
}

object SpoonLauncher {
  def createFactory: Factory = {
    val env = new StandardEnvironment
    val factory = new Factory(new DefaultCoreFactory, env)
    env.setComplianceLevel(6)
    env.setVerbose(false)
    env.setTabulationSize(4)
    env.useTabulations(true)
    env.setDebug(false)
    factory
  }
  def apply(res: CtResource): Factory = {
    val factory = createFactory
    val builder = factory.getBuilder
    builder addInputSource res
    builder.build
    factory
  }
  def apply(src: String): Factory = {
    apply(new CtVirtualFile(src, "Test.java"))
  }
}