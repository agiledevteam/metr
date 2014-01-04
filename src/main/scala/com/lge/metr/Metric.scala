package com.lge.metr

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.PrintWriter

import scala.collection.mutable.ListBuffer

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

import com.lge.metr.JavaParser.CompilationUnitContext

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

class Metric extends MetricCounter {

  import JavaModel._

  val inputs = ListBuffer[Resource]()

  def addSource(src: String) {
    inputs += new StringResource(src)
  }

  def addSource(f: File) {
    if (f.isDirectory) {
      f.listFiles.foreach(addSource(_))
    } else if (f.getPath().endsWith(".java")) {
      inputs += new FileResource(f)
    }
  }

  def parse(in: InputStream): CompilationUnitContext = {
    val input = new ANTLRInputStream(in);
    val source = new JavaLexer(input);
    val tokens = new CommonTokenStream(source);
    val p = new JavaParser(tokens);
    p.compilationUnit();
  }

  val entries = ListBuffer[MethodStatEntry]()

  def load() {
    for {
      input <- inputs
      compilationUnit = parse(input.inputStream)
      exe <- findExecutableIn(compilationUnit)
    } {
      entries += MethodStatEntry(sloc(exe).toInt, dloc(exe), cc(exe), exe.name)
    }
  }

  def generate(reportFile: File) {
    new TextGenerator(reportFile).generate(entries)
  }

  def stat: StatEntry = StatEntry(
    (1 - entries.map(_.dloc).sum / entries.map(_.sloc).sum) * 100,
    entries.map(_.cc - 1).sum + 1,
    entries.map(_.sloc).sum,
    entries.map(_.dloc).sum)

  def findExecutableIn(cu: CompilationUnitContext): List[Executable] = {
    val listener = new TreeListener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables.toList
  }
}

object Metric {
  def apply(src: String): Metric = {
    val launcher = new Metric
    launcher.addSource(src)
    launcher.load
    launcher
  }
  def apply(src: File): Metric = {
    val launcher = new Metric
    launcher.addSource(src)
    launcher.load
    launcher
  }
}