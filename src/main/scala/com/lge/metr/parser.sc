package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._
import org.parboiled.Parboiled
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.examples.java.InputUtil
import org.parboiled.support.ParseTreeUtils
import org.parboiled.Node
import org.parboiled.trees.GraphUtils
import java.util.ArrayList
import scala.collection.JavaConversions._

object parser {
  val input = """
  class Test {
     <T> Test(T t) { }
     Test() { }
     void f() {}
     int g() {}
     <T> void h(T t) {}
  }
  """                                             //> input  : String = "
                                                  //|   class Test {
                                                  //|      <T> Test(T t) { }
                                                  //|      Test() { }
                                                  //|      void f() {}
                                                  //|      int g() {}
                                                  //|      <T> void h(T t) {}
                                                  //|   }
                                                  //|   "
  val parser = Parboiled.createParser[org.parboiled.examples.java.JavaParser, java.lang.Object](classOf[org.parboiled.examples.java.JavaParser])
                                                  //> parser  : org.parboiled.examples.java.JavaParser = org.parboiled.examples.ja
                                                  //| va.JavaParser$$parboiled@18f048dc
  val result = ReportingParseRunner.run[Object](parser.CompilationUnit(), input)
                                                  //> result  : org.parboiled.support.ParsingResult[Object] = org.parboiled.suppor
                                                  //| t.ParsingResult@30b688e1

  type N = Node[Object]
  def find(a: N, path: String): Option[N] = Option(ParseTreeUtils.findNodeByPath(a, path))
                                                  //> find: (a: com.lge.metr.parser.N, path: String)Option[com.lge.metr.parser.N]
  def findMethodBodyBlocks(a: N): List[N] = {
    val nodes = new ArrayList[Node[Object]]()
    GraphUtils.collectAllNodes[Node[Object], ArrayList[Node[Object]]](a, nodes)
    val mds = nodes.filter(n => n.getLabel == "MemberDecl")
    val bodies = mds.map(md => find(md, "Seq/Met/Fir/Met/B")).flatten ++
      mds.map(md => find(md, "Seq/Voi/Fir/Met/B")).flatten ++
      mds.map(md => find(md, "Seq/Con/Met/B")).flatten ++
      mds.map(md => find(md, "Seq/Gen/Seq/Met/Fir/Met/B")).flatten ++
      mds.map(md => find(md, "Seq/Gen/Seq/Con/Met/B")).flatten
    bodies.toList
  }                                               //> findMethodBodyBlocks: (a: com.lge.metr.parser.N)List[com.lge.metr.parser.N]
                                                  //| 
  
  findMethodBodyBlocks(result.parseTreeRoot)      //> res0: List[com.lge.metr.parser.N] = List([BlockStatements], [BlockStatement
                                                  //| s], [BlockStatements], [BlockStatements], [BlockStatements])

}