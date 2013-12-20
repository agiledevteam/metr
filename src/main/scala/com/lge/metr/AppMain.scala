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
  with LocCounter with CallCounter with CCCounter {
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

  def getTargets(config: Config) =
    if (config.targets.contains("all")) {
      possibleTargets - "all"
    } else {
      config.targets
    }

  var methods: List[CtExecutable[_]] = List()

  class MethodCalculatorTask(name: String, handler: CtExecutable[_] => Any) extends Task(name) {
    def doGenerate() {
      val p = new PrintWriter(getFile)
      methods foreach { m =>
        p.println(handler(m) + "\t" + nameFor(m))
      }
      p.close
    }
  }
  val ncallsTask = new MethodCalculatorTask("ncalls", ncalls(_))
  val slocTask = new MethodCalculatorTask("sloc", sloc(_))
  val dlocTask = new MethodCalculatorTask("dloc", dloc(_))
  val ccTask = new MethodCalculatorTask("cc", cc(_))

  def fromFile(f: File): rx.lang.scala.Observable[String] =
    toScalaObservable(rx.Observable.from(asJavaIterable(Source.fromFile(f).getLines.toIterable)))

  val reportTask = Task("report") { self =>
    val names = fromFile(slocTask.getFile).map(line => line.split("\t")(1))
    val sloc = fromFile(slocTask.getFile).map(line => line.split("\t")(0).toDouble)
    val dloc = fromFile(dlocTask.getFile).map(line => line.split("\t")(0).toDouble)
    val ncalls = fromFile(ncallsTask.getFile).map(line => line.split("\t")(0).toInt)
    val stat = new Stat
    Observable.zip(names, sloc, dloc, ncalls).map((StatEntry.tupled)(_)).subscribe(
      stat.add(_),
      error => println("error: " + error),
      () => { println(stat.report); stat.exportAsText("report.txt") })
  }.dependOn(slocTask, dlocTask, ncallsTask)

  val tasks = Map(
    "report" -> reportTask,
    "ncalls" -> ncallsTask,
    "sloc" -> slocTask,
    "dloc" -> dlocTask,
    "cc" -> ccTask)

  parser.parse(args, Config(targets = Set("all"))) map { config =>
    val targets = getTargets(config).map(tasks)

    targets foreach { t =>
      println("clean " + t.getFile)
      t.clean
    }

    println("loading...")
    batch(config.src, config.deps)
    println("processed.")

    methods = factory.all[CtExecutable[_]].filter(m => !m.isImplicit && m.getBody != null)
    targets foreach { t =>
      println("generating " + t.getFile + " ...")
      t.generate
    }
    println("done.")
  } getOrElse {
  }

}