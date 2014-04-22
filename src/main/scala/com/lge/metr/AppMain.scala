package com.lge.metr

import java.io.File
import scala.Array.canBuildFrom
import scala.collection.mutable.ListBuffer
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import scala.swing.ListView
import java.awt.Dimension
import scala.swing.event.MouseClicked

object LVTest extends SimpleSwingApplication {

  def top = new MainFrame {
    contents = myListView
    size = new Dimension(200, 200)
  }

  val myListView = new ListView[String]() {
    val myListBuffer = ListBuffer("Paris", "New York", "Tokyo", "Berlin", "Copenhagen")
    listData = myListBuffer
    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked => {
        myListBuffer += "Slough"
        listData = myListBuffer
      }
    }
  }
}
//  println("pwd:"+new File("").getAbsolutePath)
//  parser.parse(args, Config(null, new File("report.txt"), false, "HEAD", new File("output"))) map { config =>
//    if (config.trend) {
//      new Trend(config.src, config.dest, config.debug).run(config.commit)
//    } else {
//      val metr = new Metric
//      metr.addSource(config.src)
//
//      print("loading...")
//      metr.load
//      println("done")
//
//      print("generating...")
//      metr.generate(config.out)
//      println("done")
//    }
//  } getOrElse {
//    println("Unknown options: "+args.mkString)
//  }
//
//}