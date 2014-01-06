package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom

case class Config(src: File, out: File, trend: Boolean, dest: File, debug: Boolean = false)

object AppMain extends App {
  val parser = new scopt.OptionParser[Config]("metr") {
    head("metr", "1.0")

    opt[String]('s', "src") required () valueName ("<file or directory>") action { (x, c) =>
      c.copy(src = new File(x))
    } text ("src is a file to analyize or a directory which is a root of source files")
    opt[String]('o', "output") optional () valueName ("<output file path>") action { (x, c) =>
      c.copy(out = new File(x))
    }
    opt[Unit]('t', "trend") optional () valueName ("look up all commits from current branch") action { (_, c) =>
      c.copy(trend = true)
    }
    opt[Unit]('g', "debug") hidden () action { (_, c) =>
    c.copy(debug = true)
    }
    opt[String]('d', "dest") optional () valueName ("destination for trend") action { (x, c) =>
      c.copy(dest = new File(x))
    }
    checkConfig { c =>
      if (c.trend && c.dest.exists && !c.dest.isDirectory)
        failure(s"${c.dest} should be a directory.")
      else success
    }
  }
  println("pwd:" + new File("").getAbsolutePath)
  parser.parse(args, Config(null, new File("report.txt"), false, new File("output"))) map { config =>
    if (config.trend) {
      new Trend(config.src, config.dest).run(config.debug)
    } else {
      val metr = new Metric
      metr.addSource(config.src)

      print("loading...")
      metr.load
      println("done")

      print("generating...")
      metr.generate(config.out)
      println("done")
    }
  } getOrElse {
    println("Unknown options: " + args.mkString)
  }

}