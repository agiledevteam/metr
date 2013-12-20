package com.lge.metr

import java.io.File

import spoon.reflect.Factory
import spoon.support.DefaultCoreFactory
import spoon.support.StandardEnvironment
import spoon.support.builder.CtResource

class SpoonLauncher {
  val factory = SpoonLauncher.createFactory
  
  def batch(src: Seq[File], deps: Seq[File]) {
    ClasspathHolder.additionalClasspath = deps.mkString(File.pathSeparator)
    val builder = factory.getBuilder
    src foreach (builder.addInputSource(_))
    builder.build
  }
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
}