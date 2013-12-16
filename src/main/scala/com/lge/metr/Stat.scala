package com.lge.metr

import scala.collection.mutable.ListBuffer

case class StatEntry(name: String, sloc: Double, dloc: Double, ncalls: Int)

class Stat {
  var entries: ListBuffer[StatEntry] = ListBuffer()
  def add(entry: StatEntry) {
    entries += entry
  }
  def report: String = {
    val tloc = entries.map(_.sloc).sum
    val aloc = entries.map(e => e.dloc * e.ncalls).sum
    val rate = 1 - tloc/aloc
    s"Total method loc: $tloc\nTotal method dloc: $aloc\nCode compression rate: ${rate/100}"
  }
}