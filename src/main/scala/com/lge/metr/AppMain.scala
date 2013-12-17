package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom
import spoon.AbstractLauncher
import spoon.reflect.Factory
import spoon.support.DefaultCoreFactory
import spoon.support.JavaOutputProcessor
import spoon.support.StandardEnvironment
import spoon.processing.Environment
import spoon.processing.Builder
import spoon.support.builder.SpoonBuildingManager
import spoon.reflect.declaration.CtExecutable

case class Config(src: Seq[File] = Seq(), deps: Seq[File] = Seq())

object AppMain extends AbstractLauncher with App with LocCounter with CallCounter {
  val factory = getFactory

  val parser = new scopt.OptionParser[Config]("metr") {
    head("metr", "1.0")

    opt[String]('s', "src") required () valueName ("<separated list of file or directory>") action { (x, c) =>
      c.copy(src = c.src ++ x.split(File.pathSeparator).map(new File(_)))
    } text ("src is a file to analyize or a directory which is a root of source files")
    opt[String]('d', "deps") optional () valueName (s"separated list of jar-files") action { (x, c) =>
      c.copy(deps = c.deps ++ x.split(File.pathSeparator).map(new File(_)))
    }
  }

  // parser.parse returns Option[C]
  parser.parse(args, Config()) map { config =>
    ClasspathHolder.additionalClasspath = config.deps.mkString(File.pathSeparator)
    val builder = factory.getBuilder
    config.src foreach (builder.addInputSource(_))
    builder.build
    val stat = new Stat
    factory.all[CtExecutable[_]].foreach { m =>
      if (!m.isImplicit && m.getBody != null) {
        val name = nameFor(m)
        val loc1 = sloc(m)
        val loc2 = dloc(m)
        val invokes = ncalls(m)
        if (invokes > 1)
          println(s"${name}\t$loc1\t$loc2\t$invokes")
        stat.add(StatEntry(name, loc1, loc2, invokes))
      }
    }
    println(stat.report)

  } getOrElse {
  }

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

}