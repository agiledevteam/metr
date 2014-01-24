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
import org.parboiled.buffers.InputBuffer
import scala.language.implicitConversions

class ParboiledJavaProcessor extends JavaProcessor {
  type N = Node[Object]
  implicit class MyNode(val n: N) {
    def child(prefix: String): N =
      n.getChildren.find(_.getLabel.startsWith(prefix)).getOrElse(null)
    def get(index: Int): N =
      n.getChildren.lift(index).getOrElse(null)
    def all(prefix: String): Seq[N] =
      n.getChildren.filter(_.getLabel.startsWith(prefix))
  }

  override def process(in: String): CompUnit = {
    val parser = Parboiled.createParser[JP, Object](classOf[JP])
    val result = new ReportingParseRunner[Object](parser.CompilationUnit).run(in)
    val methods = allMethods(result.parseTreeRoot)
    CompUnit(methods.map(b => toMethod(b, result.inputBuffer)))
  }

  def toMethod(n: N, buffer: InputBuffer): Method = {
    val name = ParseTreeUtils.getNodeText(n.child("id"), buffer)
    Method(name, toStmt(n.child("block")))
  }

  def toStmt(n: N): Stmt = n.getLabel match {
    case "block" => 
      BlockStmt(List() ++ n.getChildren.map(toStmt))
    case "if" => 
      IfStmt(toStmt(n.get(1)), Option(n.get(2)).map(toStmt))
    case kw @ ("for" | "while" | "do" | "for-each" ) =>
      LoopStmt(kw, toStmt(n.get(0)))
    case "try" =>
      TryStmt(toStmt(n.child("block")), 
          List() ++ n.all("catch").map(_.child("block")).map(toStmt),
          Option(n.child("finally")).map(_.child("block")).map(toStmt))
    case "switch" =>
      SwitchStmt(n.all("case-block").map { cb =>
        val t = cb.getChildren.tail
        if (t.size == 0) BlockStmt(List())
        else if (t.size == 1) toStmt(t.head)
        else BlockStmt(List() ++ t.map(toStmt))
      })
    case "synchronized" =>
      SyncStmt(toStmt(n.child("block")))
    case ";" =>
      BlockStmt(List())
    case _ => 
      //println(n.getLabel); 
      OtherStmt()
  }

  def debug(n: N) {
    def tree(ind: Int, n: N) {
      println(" " * ind + ind + ":" + n.getLabel)
      n.getChildren.foreach(tree(ind + 1, _))
    }
    tree(0, n)
  }

  def allMethods(a: N): List[N] = {
//    debug(a)
    val nodes = new ArrayList[Node[Object]]()
    GraphUtils.collectAllNodes[Node[Object], ArrayList[Node[Object]]](a, nodes)
    List() ++ nodes.filter(n => n.getLabel == "method" && n.child("block") != null)
  }
}