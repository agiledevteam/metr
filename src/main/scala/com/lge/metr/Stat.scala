package com.lge.metr

class Stat(val sloc: Int, val floc: Double) {
  val codefat = if (sloc == 0) 0 else 100 * floc / sloc;
}