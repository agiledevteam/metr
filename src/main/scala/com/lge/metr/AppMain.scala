package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom

case class Config(
  src: Seq[File] = Seq(),
  deps: Seq[File] = Seq(),
  targets: Set[String])

object AppMain extends App {
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
      else failure("targets should be one of "+possibleTargets)
    }
    opt[String]('f', "file") optional () valueName ("files for config") action { (x, c) =>
      c // TODO read config from specified file
    }
  }

  parser.parse(args, Config(targets = Set("all"))) map { config =>
    val launcher = new SpoonLauncher(config)
    print("loading...")
    launcher.loadAll
    println("done")

    print("generating...")
    launcher.generate("report.txt")
    println("done")
  } getOrElse {
  }

}