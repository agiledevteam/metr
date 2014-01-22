package com.lge.metr

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

import scala.collection.mutable.ListBuffer

abstract class Resource {
  def inputStream: InputStream
}

class StringResource(src: String) extends Resource {
  def inputStream = new ByteArrayInputStream(src.getBytes)
}

class FileResource(f: File) extends Resource {
  def inputStream = new FileInputStream(f)
}

class Metric extends MetricCounter {
  val java = new JavaProcessor()

  val inputs = ListBuffer[Resource]()

  def addSource(src: Resource): Unit =
    inputs += src

  def addSource(src: String): Unit =
    addSource(new StringResource(src))

  def addSource(f: File) {
    if (f.isDirectory) {
      f.listFiles.foreach(addSource(_))
    } else if (f.getPath().endsWith(".java")) {
      inputs += new FileResource(f)
    }
  }

  val entries = ListBuffer[StatEntry]()

  def load: Unit =
    inputs.foreach { in =>
      val cu = java.process(in.inputStream)
      entries ++= cu.exes.map(exe => StatEntry(cc(exe), sloc(exe).toInt, dloc(exe)))
    }

  def generate(reportFile: File) {
    new TextGenerator(reportFile).generate(entries)
  }

  def stat: StatEntry = StatEntry(
    entries.map(_.cc - 1).sum + 1,
    entries.map(_.sloc).sum,
    entries.map(_.dloc).sum)
}

object Metric {
  def apply(src: String): Metric = apply(new StringResource(src))
  def apply(src: InputStream): Metric = apply(new Resource() { val inputStream = src; })
  def apply(src: File): Metric = {
    val launcher = new Metric
    launcher.addSource(src)
    launcher.load
    launcher
  }
  def apply(src: Resource): Metric = {
    val launcher = new Metric
    launcher.addSource(src)
    launcher.load
    launcher
  }
}