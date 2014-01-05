package com.lge.metr

import java.io.PrintWriter
import java.io.File

class TextGenerator(out: File) {
  def generate(seq: Seq[Values]): Unit = {
    val printer = new PrintWriter(out)
    seq.foreach(v => printer.println(v.values.mkString("\t")))
    printer.close
  }
}