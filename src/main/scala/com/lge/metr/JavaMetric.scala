package com.lge.metr

import com.lge.metr.JavaParser.CompilationUnitContext
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class JavaMetric extends MetricCounter {
  import JavaModel._

  def process(input: Resource): Seq[MethodStatEntry] = {
    val cu = parse(input.inputStream)
    for (exe <- findExecutableIn(cu)) yield MethodStatEntry(sloc(exe).toInt, dloc(exe), cc(exe), exe.name)
  }

  def parse(in: InputStream): CompilationUnitContext = {
    val input = new ANTLRInputStream(new InputStreamReader(in, StandardCharsets.UTF_8))
    val source = new JavaLexer(input)
    val tokens = new CommonTokenStream(source)
    val p = new JavaParser(tokens)
    p.compilationUnit()
  }

  def findExecutableIn(cu: CompilationUnitContext): Seq[Executable] = {
    val listener = new TreeListener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables.toList
  }
}