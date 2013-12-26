package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

trait LocCounter {
  import JavaModel._

  def ifElseChain(ifStmt: IfStmt): List[Stmt] = {
    var cur: Stmt = ifStmt
    val list = ListBuffer[Stmt]()
    while (cur != null) {
      cur match {
        case IfStmt(thenPart, elsePart) =>
          list += thenPart
          cur = elsePart
        case _ =>
          list += cur
          cur = null
      }
    }
    list.toList
  }

  private def loc(stmt: Stmt)(implicit df: Double): Double = stmt match {
    case null =>
      0
    case ifStmt: IfStmt =>
      ifElseChain(ifStmt).map(thenPart => 1 + loc(thenPart) * df).sum
    case SwitchStmt(cases) =>
      1 + cases.map(c => loc(c) * df + 1).sum
    case LoopStmt(keyword, body) =>
      (if (keyword == "do") 2 else 1) + loc(body) * df
    case BlockStmt(statements) =>
      statements.map(loc(_)).sum
    case SyncStmt(block) =>
      1 + loc(block)
    case TryStmt(block, catchers, finalizer) =>
      1 + loc(block) + catchers.map(loc(_) + 1).sum + (
        if (finalizer != null)
          loc(finalizer) + 1
        else
          0)
    case s =>
      1
  }

  def sloc(stmt: Executable): Double = loc(stmt.body)(1)
  def dloc(stmt: Executable): Double = loc(stmt.body)(0.5)
}