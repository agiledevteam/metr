package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom

case class Config(src: File)

object AppMain extends App {
  val parser = new scopt.OptionParser[Config]("metr") {
    head("metr", "1.0")

    opt[String]('s', "src") required () valueName ("<file or directory>") action { (x, c) =>
      c.copy(src = new File(x))
    } text ("src is a file to analyize or a directory which is a root of source files")
    opt[String]('f', "file") optional () valueName ("files for config") action { (x, c) =>
      c // TODO read config from specified file
    }
  }

  parser.parse(args, Config(null)) map { config =>
    val launcher = new SpoonLauncher
    launcher.addSource(config.src)

    print("loading...")
    launcher.load
    println("done")

    print("generating...")
    launcher.generate("report.txt")
    println("done")
  } getOrElse {
  }

}