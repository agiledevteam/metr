package com.lge.metr

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

import scala.collection.mutable.ListBuffer

case class MethodStatEntry(sloc: Int, dloc: Double, cc: Int, name: String) extends Values {
  def values: Seq[Any] = Seq(sloc, dloc, cc, name)
}

abstract class Resource {
  def inputStream: InputStream
}

class StringResource(src: String) extends Resource {
  def inputStream = new ByteArrayInputStream(src.getBytes)
}

class FileResource(f: File) extends Resource {
  def inputStream = new FileInputStream(f)
}

class Metric {
  val java = new JavaMetric()

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

  val entries = ListBuffer[MethodStatEntry]()

  def load: Unit =
    for (input <- inputs) entries ++= java.process(input)

  def generate(reportFile: File) {
    new TextGenerator(reportFile).generate(entries)
  }

  def stat: StatEntry = StatEntry(
    (1 - entries.map(_.dloc).sum / entries.map(_.sloc).sum) * 100,
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