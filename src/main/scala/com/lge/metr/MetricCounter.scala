package com.lge.metr

import scala.collection.JavaConversions._

trait MetricCounter {
  import JavaModel._

  def cc(exe: Executable): Int = cc(exe.body) + 1
  def sloc(stmt: Executable): Int = loc(stmt.body)(1).toInt
  def dloc(stmt: Executable): Double = loc(stmt.body)(0.5)

  private def cc(stmt: Stmt): Int = stmt match {
    case IfStmt(t, f) => 1 + (List(t) ++ f).map(cc(_)).sum
    case SwitchStmt(cases) => cases.map(cc(_) + 1).sum
    case LoopStmt(_, body) => cc(body) + 1
    case BlockStmt(statements) => statements.map(cc(_)).sum
    case SyncStmt(body) => cc(body)
    case TryStmt(body, catchers, finalizer) =>
      ((body +: catchers) ++ finalizer).map(cc(_)).sum
    case _ => 0
  }

  def ifElseChain(stmt: Stmt): List[Stmt] = stmt match {
    case IfStmt(thenPart, Some(elsePart)) => thenPart :: ifElseChain(elsePart)
    case IfStmt(thenPart, None) => List(thenPart)
    case _ => List(stmt)
  }

  private def loc(stmt: Stmt)(implicit df: Double): Double = stmt match {
    case IfStmt(_, _) => ifElseChain(stmt).map(1 + loc(_) * df).sum
    case SwitchStmt(cases) => 1 + cases.map(1 + loc(_) * df).sum
    case LoopStmt(kw, body) => (if (kw == "do") 2 else 1) + loc(body) * df
    case BlockStmt(statements) => statements.map(loc(_)).sum
    case SyncStmt(body) => 1 + loc(body)
    case TryStmt(body, catchers, finalizer) =>
      ((body +: catchers) ++ finalizer).map(loc(_) + 1).sum
    case _ => 1
  }

}
