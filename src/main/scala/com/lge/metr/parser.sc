package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._

object parser {
"gitdir: ../../.git/modules/samples/github-android".span(_ != ' ')
                                                  //> res0: (String, String) = (gitdir:," ../../.git/modules/samples/github-androi
                                                  //| d")
  val timestamp = "1386638707 42b0768db48cbb3513a19985b8c4982b6efa8da1".split(" ")(0)
                                                  //> timestamp  : String = 1386638707
  val dateFormatter = new java.text.SimpleDateFormat
                                                  //> dateFormatter  : java.text.SimpleDateFormat = java.text.SimpleDateFormat@a94
                                                  //| 27c06
  dateFormatter.format(timestamp.toLong * 1000)   //> res1: String = 12/10/13 10:25 AM
  
  new File("/Users/jooyunghan/work/scala/metr/samples/github-android/.git").cat.!!
                                                  //> res2: String = "gitdir: ../../.git/modules/samples/github-android
                                                  //| "
  Paths.get("/Users").relativize(Paths.get("/Users/jooyunghan/work") )
                                                  //> res3: java.nio.file.Path = jooyunghan/work
}