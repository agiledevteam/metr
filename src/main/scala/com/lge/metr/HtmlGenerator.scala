package com.lge.metr

import scala.io.Source
import java.nio.file.Path
import java.net.URI
import java.io.File
import java.io.PrintWriter

class HtmlGenerator(out: File) {
  def generate(seq: Seq[(Commit, StatEntry)]): Unit = {
    val cn = seq.map({case (c,e) =>  s"[${c.timestamp}, ${e.cn}]"}).mkString(",\n")
    val loc = seq.map({case (c,e) =>  s"[${c.timestamp}, ${e.sloc}]"}).mkString(",\n")
    val printer = new PrintWriter(out)
    Source.fromInputStream(getClass.getResourceAsStream("trend.html"))(io.Codec("UTF-8")).getLines foreach { line =>
      if (line.contains("CN_DATA"))
        printer.println(line.replace("CN_DATA", cn))
      else if (line.contains("LOC_DATA"))
        printer.println(line.replace("LOC_DATA", loc))
      else
        printer.println(line)
    }
    printer.close
  }
}