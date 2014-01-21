package com.lge.metr

import scala.io.Source
import java.nio.file.Path
import java.net.URI
import java.io.File
import java.io.PrintWriter
import scala.util.matching.Regex.Match

class HtmlGenerator(out: File) {
  def generate(seq: Seq[(Commit, StatEntry)]): Unit = {
    val variablePattern = """DATA((_SLOC|_TIME|_CN)+)""".r

    lazy val sloc = seq.map({ case (c, e) => e.sloc })
    lazy val dloc = seq.map({ case (c, e) => e.dloc })
    lazy val cc = seq.map({ case (c, e) => e.cc })
    lazy val cn = seq.map({ case (c, e) => e.cn })
    lazy val time = seq.map({ case (c, e) => c.timestamp })

    def map(v: String): Seq[Any] = v match {
      case "SLOC" => sloc
      case "DLOC" => dloc
      case "CC" => cc
      case "CN" => cn
      case "TIME" => time
    }

    def substitute(m: Match): String = {
      val vars = m.group(1).split('_').filterNot(_.isEmpty)
      if (vars.size == 1) {
        map(vars(0)) mkString ",\n"
      } else {
        map(vars(0)) zip map(vars(1)) map { p => s"[${p._1},${p._2}]" } mkString ",\n"
      }
    }

    val printer = new PrintWriter(out)
    Source.fromInputStream(getClass.getResourceAsStream("/html/trend.html"))(io.Codec("UTF-8")).getLines foreach { line =>
      printer.println(variablePattern replaceAllIn (line, substitute(_)))
    }
    printer.close
  }
}