package com.lge.metr

import java.io.InputStream
import com.lge.metr.JavaModel._
import org.parboiled.trees.GraphUtils
import org.parboiled.support.ParseTreeUtils
import org.parboiled.Parboiled
import org.parboiled.Node
import org.parboiled.parserunners.ReportingParseRunner
import java.util.ArrayList
import org.parboiled.examples.java.{ JavaParser => JP }
import scala.collection.JavaConversions._

class ParboiledJavaProcessor extends JavaProcessor {
  type N = Node[Object]
  override def process(in: String): CompUnit = {
    val parser = Parboiled.createParser[JP, Object](classOf[JP])
    val result = new ReportingParseRunner[Object](parser.CompilationUnit).run(in)
    val methods = findMethodBodyBlocks(result.parseTreeRoot)
    CompUnit(methods.map(b => Method("f", toStmt(b))))
  }

  def toStmt(n: N): Stmt = {
    if (n.getLabel == "Block") {
      BlockStmt(List())
    } else {
      BlockStmt(List())
    }
  }

  def statements(b: N): List[Stmt] = {
    debug(b)
    List()
  }

  def debug(n: N) {
    def tree(ind: Int, n: N) {
      println(" " * ind + ind+":"+n.getLabel)
      n.getChildren.foreach(tree(ind + 1, _))
    }
    tree(0, n)
  }

  def find(a: N, path: String): Option[N] =
    Option(ParseTreeUtils.findNodeByPath(a, path))

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
  }
}