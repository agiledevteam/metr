package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._

object parser {
  new HtmlGenerator(new File("")).generate(List())//> java.io.BufferedInputStream@71ce109a
  val sysClassLoader = java.lang.ClassLoader.getSystemClassLoader()
                                                  //> sysClassLoader  : ClassLoader = sun.misc.Launcher$AppClassLoader@7dc4cd9
  getClass().getResource("/com/lge/metr/trend.html")
                                                  //> res0: java.net.URL = file:/Users/jooyunghan/work/scala/metr/target/scala-2.1
                                                  //| 0/classes/com/lge/metr/trend.html
"gitdir: ../../.git/modules/samples/github-android".span(_ != ' ')
                                                  //> res1: (String, String) = (gitdir:," ../../.git/modules/samples/github-androi
                                                  //| d")
  val timestamp = "1386638707 42b0768db48cbb3513a19985b8c4982b6efa8da1".split(" ")(0)
                                                  //> timestamp  : String = 1386638707
  val dateFormatter = new java.text.SimpleDateFormat
                                                  //> dateFormatter  : java.text.SimpleDateFormat = java.text.SimpleDateFormat@a94
                                                  //| 27c06
  dateFormatter.format(timestamp.toLong * 1000)   //> res2: String = 12/10/13 10:25 AM
  
  new File("/Users/jooyunghan/work/scala/metr/samples/github-android/.git").cat.!!
                                                  //> res3: String = "gitdir: ../../.git/modules/samples/github-android
                                                  //| "
  Paths.get("/Users").relativize(Paths.get("/Users/jooyunghan/work") )
                                                  //> res4: java.nio.file.Path = jooyunghan/work
}