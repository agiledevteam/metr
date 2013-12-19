package com.lge.metr

import spoon.AbstractLauncher
import spoon.support.StandardEnvironment
import spoon.reflect.Factory
import spoon.support.DefaultCoreFactory
import java.io.File

class SpoonLauncher extends AbstractLauncher {

  override def createFactory: Factory = {
    val env = new StandardEnvironment
    val factory = new Factory(new DefaultCoreFactory, env)

    env.setComplianceLevel(6)
    env.setVerbose(false)
    env.setTabulationSize(4)
    env.useTabulations(true)
    env.setDebug(false)
    factory
  }

  def process(src: Seq[File], deps: Seq[File]) {
    ClasspathHolder.additionalClasspath = deps.mkString(File.pathSeparator)

    val builder = getFactory.getBuilder
    src foreach (builder.addInputSource(_))
    builder.build
  }
}