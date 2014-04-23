package com.lge.metr

class Stat(val exes: Array[ExeStat]) {
  val sloc = exes.map(_.sloc).sum
  val floc = exes.map(_.floc).sum
  val codefat = if (sloc == 0) 0 else 100 * floc / sloc;
}

class ExeStat(val typeName: String,
  val methodName: String,
  val sloc: Int,
  val floc: Double) {
  val codefat = if (sloc == 0) 0 else 100 * floc / sloc;
}
