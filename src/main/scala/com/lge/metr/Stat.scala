package com.lge.metr

import scala.collection.mutable.ListBuffer
import java.io.File
import java.io.PrintWriter

case class StatEntry(name: String, sloc: Double, dloc: Double, ncalls: Int) {
  def asText: String = "%s\t%.0f\t%.2f\t%d".format(name, sloc, dloc, ncalls)
}

class Stat {
  var entries: ListBuffer[StatEntry] = ListBuffer()
  def add(entry: StatEntry) {
    entries += entry
  }

  val headerText = "method\tsloc\tdloc\tncalls"
  val separatorText = "=" * 30

  def exportAsText(filename: String) {
    val printer = new PrintWriter(new File(filename))
    printer.println(report)
    printer.println(separatorText)
    printer.println(headerText)
    entries foreach { entry =>
      printer.println(entry.asText)
    }
    printer.close
  }
  def report: String = {
    val tloc = entries.map(_.sloc).sum
    val aloc = entries.map(e => e.dloc * e.ncalls).sum
    val rate = 1 - tloc / aloc
    "Total method loc: %.0f\nTotal method aloc: %.2f\nCode compression rate: %.2f%%".
      format(tloc, aloc, rate * 100)
  }
}