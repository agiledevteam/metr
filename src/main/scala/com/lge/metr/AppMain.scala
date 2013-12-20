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
import spoon.reflect.declaration.CtMethod
import scala.io.Source
import java.io.PrintWriter
import rx.lang.scala.Observable
import scala.collection.JavaConversions.asJavaIterable
import rx.lang.scala.ImplicitFunctionConversions._
import scala.collection.mutable.ListBuffer

case class Config(
  src: Seq[File] = Seq(),
  deps: Seq[File] = Seq(),
  targets: Set[String])

object AppMain extends SpoonLauncher with App
  with LocCounter with CallCounter with CCCounter with Naming {
  val possibleTargets = Set("all", "cc", "dloc", "sloc", "ncalls", "report")

  val parser = new scopt.OptionParser[Config]("metr") {
    head("metr", "1.0")

    opt[String]('s', "src") required () valueName ("<separated list of file or directory>") action { (x, c) =>
      c.copy(src = c.src ++ x.split(File.pathSeparator).map(new File(_)))
    } text ("src is a file to analyize or a directory which is a root of source files")
    opt[String]('d', "deps") optional () valueName (s"separated list of jar-files") action { (x, c) =>
      c.copy(deps = c.deps ++ x.split(File.pathSeparator).map(new File(_)))
    }
    opt[String]('t', "target") optional () valueName ("target metric list ") action { (x, c) =>
      c.copy(targets = x.split(File.pathSeparator).toSet)
    } validate { x =>
      if (x.split(File.pathSeparator).forall(possibleTargets.contains(_))) success
      else failure("targets should be one of " + possibleTargets)
    }
    opt[String]('f', "file") optional () valueName ("files for config") action { (x, c) =>
      c // TODO read config from specified file
    }
  }

  parser.parse(args, Config(targets = Set("all"))) map { config =>
    println("loading...")
    batch(config.src, config.deps)

    val methods = factory.all[CtExecutable[_]].filter(m => !m.isImplicit && m.getBody != null)

    val handlers: List[CtExecutable[_] => Any] = List(sloc(_), dloc(_), ncalls(_), cc(_), nameFor(_))
    println("generating...")
    val p = new PrintWriter("report.txt")
    methods foreach { m =>
      p.println(handlers.map(h => h(m)).mkString("\t"))
    }
    p.close

    println("done.")
  } getOrElse {
  }

}