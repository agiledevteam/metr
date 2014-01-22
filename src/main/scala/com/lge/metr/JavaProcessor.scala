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

class JavaProcessor {

  def process(input: InputStream): CompUnit = {
    val cu = parse(input)
    CompUnit(findExecutableIn(cu))
  }

  def parse(input: InputStream): CompilationUnitContext = {
    val input2 = new ANTLRInputStream(new InputStreamReader(input, StandardCharsets.UTF_8))
    val source = new JavaLexer(input2)
    val tokens = new CommonTokenStream(source)
    val p = new JavaParser(tokens)
    p.compilationUnit()
  }

  def findExecutableIn(cu: CompilationUnitContext): List[Executable] = {
    val listener = new TreeListener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables.toList
  }
}