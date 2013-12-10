package com.lge.metr

class Launcher(args:Array[String]) extends spoon.Launcher(args) 

object Launcher {
  def main(args: Array[String]) = new Launcher(args).run()
}
