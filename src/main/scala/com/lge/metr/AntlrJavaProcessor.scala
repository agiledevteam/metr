package com.lge.metr

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

import com.lge.metr.JavaModel.CompUnit
import com.lge.metr.JavaModel.Executable
import com.lge.metr.JavaParser.CompilationUnitContext

class AntlrJavaProcessor extends JavaProcessor {
  override def process(input: String): CompUnit = {
    val stream = new ANTLRInputStream(input)
    process(stream)
  }

  override def process(input: InputStream): CompUnit = {
    val stream = new ANTLRInputStream(input)
    process(stream)
  }

  def process(stream: ANTLRInputStream): CompUnit = {
    val source = new JavaLexer(stream)
    val tokens = new CommonTokenStream(source)
    val p = new JavaParser(tokens)
    val cu = p.compilationUnit()
    CompUnit(findExecutableIn(cu))
  }

  def findExecutableIn(cu: CompilationUnitContext): Seq[Executable] = {
    val listener = new TreeListener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables
  }
}