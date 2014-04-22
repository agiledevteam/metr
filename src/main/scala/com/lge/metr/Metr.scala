package com.lge.metr

import java.io.File

object Metr extends MetricCounter {
    def metr(f: File): Stat = {
      val processor = new ParboiledJavaProcessor()
      val compUnit = processor.process(f)
      val sumSloc = compUnit.exes.map(sloc(_)).sum
      val sumDloc = compUnit.exes.map(dloc(_)).sum
      print(f, sumSloc, sumDloc)
      return new Stat(sumSloc, sumSloc - sumDloc)
    }
}
