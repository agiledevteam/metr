package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom

case class Config(src: File, out: File)

object AppMain extends App {
  val parser = new scopt.OptionParser[Config]("metr") {
    head("metr", "1.0")

    opt[String]('s', "src") required () valueName ("<file or directory>") action { (x, c) =>
      c.copy(src = new File(x))
    } text ("src is a file to analyize or a directory which is a root of source files")
    opt[String]('o', "output") optional () valueName ("<output file path>") action { (x, c) =>
      c.copy(out = new File(x))
    }
  }

  parser.parse(args, Config(null, new File("report.txt"))) map { config =>
    val metr = new Metric
    metr.addSource(config.src)

    print("loading...")
    metr.load
    println("done")

    print("generating...")
    metr.generate(config.out)
    println("done")
  } getOrElse {
  }

}