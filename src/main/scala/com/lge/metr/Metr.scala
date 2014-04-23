package com.lge.metr

import java.io.File

object Metr extends MetricCounter {
    def metr(f: File): Stat = {
      val processor = new ParboiledJavaProcessor()
      val compUnit = processor.process(f)
      return new Stat(compUnit.exes.map(toExeStat(_)).toArray)
    }
    def toExeStat(exe: JavaModel.Executable): ExeStat = {
        val s = sloc(exe)
        val d = dloc(exe)
        new ExeStat("", exe.name, s, s-d)
    }
}
