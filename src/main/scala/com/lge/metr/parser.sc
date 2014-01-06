package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk

object parser {
   val r = new FileRepositoryBuilder().setGitDir(new File("/Users/jooyunghan/work/scala/metr/.git/modules/samples/github-android")).build
                                                  //> r  : <error> = Repository[/Users/jooyunghan/work/scala/metr/.git/modules/sam
                                                  //| ples/github-android]
   val h = r.getRef("refs/heads/master")          //> h  : <error> = Ref[refs/heads/master=c6bdc122f2e8ac3594d7b83b998bb06bb774848
                                                  //| 8]
   val w = new RevWalk(r)                         //> w  : <error> = org.eclipse.jgit.revwalk.RevWalk@5f3c750c
   w.parseCommit(h.getObjectId)                   //> res0: <error> = commit c6bdc122f2e8ac3594d7b83b998bb06bb7748488 1386790622 -
                                                  //| ----p
  new HtmlGenerator(new File("")).generate(List())//> java.io.FileNotFoundException:  (No such file or directory)
                                                  //| 	at java.io.FileOutputStream.open(Native Method)
                                                  //| 	at java.io.FileOutputStream.<init>(FileOutputStream.java:212)
                                                  //| 	at java.io.FileOutputStream.<init>(FileOutputStream.java:165)
                                                  //| 	at java.io.PrintWriter.<init>(PrintWriter.java:263)
                                                  //| 	at com.lge.metr.HtmlGenerator.generate(HtmlGenerator.scala:14)
                                                  //| 	at com.lge.metr.parser$$anonfun$main$1.apply$mcV$sp(com.lge.metr.parser.
                                                  //| scala:14)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at com.lge.metr.parser$.main(com.lge.metr.parser.scala:9)
                                                  //| 	at com.lge.metr.parser.main(com.lge.metr.parser.scala)
  val sysClassLoader = java.lang.ClassLoader.getSystemClassLoader()
  getClass().getResource("/com/lge/metr/trend.html")
"gitdir: ../../.git/modules/samples/github-android".span(_ != ' ')
  val timestamp = "1386638707 42b0768db48cbb3513a19985b8c4982b6efa8da1".split(" ")(0)
  val dateFormatter = new java.text.SimpleDateFormat
  dateFormatter.format(timestamp.toLong * 1000)
  
  new File("/Users/jooyunghan/work/scala/metr/samples/github-android/.git").cat.!!
  Paths.get("/Users").relativize(Paths.get("/Users/jooyunghan/work") )
}