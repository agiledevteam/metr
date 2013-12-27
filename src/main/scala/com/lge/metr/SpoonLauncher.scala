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

import JavaModel.Executable

class SpoonLauncher extends LocCounter with CCCounter {

  import JavaModel._

  abstract class Resource {
    def inputStream: InputStream
  }
  class StringResource(src: String) extends Resource {
    def inputStream = new ByteArrayInputStream(src.getBytes)
  }
  class FileResource(f: File) extends Resource {
    def inputStream = new FileInputStream(f)
  }

  val input = ListBuffer[Resource]()

  def addSource(src: String) {
    input += new StringResource(src)
  }
  def addSource(f: File) {
    if (f.isDirectory) {
      f.listFiles.foreach(addSource(_))
    } else if (f.getPath().endsWith(".java")) {
      input += new FileResource(f)
    }
  }

  def parse(in: InputStream): CompilationUnitContext = {
    val input = new ANTLRInputStream(in);
    val source = new JavaLexer(input);
    val tokens = new CommonTokenStream(source);
    val p = new JavaParser(tokens);
    p.compilationUnit();
  }

  var compilationUnits: List[CompilationUnitContext] = List()

  def load() {
    compilationUnits = input.toList map { i =>
      parse(i.inputStream)
    }
  }

  def generate(reportFile: File) {
    val handlers: List[Executable => Any] = List(sloc(_), dloc(_), cc(_), m => m.name)
    val p = new PrintWriter(reportFile)
    allExecutables foreach { e =>
      p.println(handlers.map(h => h(e)).mkString("\t"))
    }
    p.close
  }

  def allExecutables: List[Executable] = {
    compilationUnits.flatMap(findExecutableIn _)
  }

  def findExecutableIn(cu: CompilationUnitContext): List[Executable] = {
    val listener = new TreeListener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables.toList
  }
}

object SpoonLauncher {
  def apply(src: String): SpoonLauncher = {
    val launcher = new SpoonLauncher
    launcher.addSource(src)
    launcher.load
    launcher
  }
  def apply(src: File): SpoonLauncher = {
    val launcher = new SpoonLauncher
    launcher.addSource(src)
    launcher.load
    launcher
  }
}