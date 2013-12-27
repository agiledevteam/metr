package com.lge.metr

import scala.language.implicitConversions

trait LocCounter {
  import JavaModel._

  def ifElseChain(stmt: Stmt): List[Stmt] = stmt match {
    case IfStmt(thenPart, elsePart) => thenPart :: elsePart.flatMap(ifElseChain(_))
    case _ => List(stmt)
  }

  private def loc(stmt: Stmt)(implicit df: Double): Double = stmt match {
    case IfStmt(_, _) => ifElseChain(stmt).map(1 + loc(_) * df).sum
    case SwitchStmt(cases) => 1 + cases.map(1 + loc(_) * df).sum
    case LoopStmt(kw, body) => (if (kw == "do") 2 else 1) + loc(body) * df
    case BlockStmt(statements) => statements.map(loc(_)).sum
    case SyncStmt(body) => 1 + loc(body)
    case TryStmt(body, catchers, finalizer) =>
      (body :: catchers ::: finalizer).map(loc(_) + 1).sum
    case s => 1
  }

  def sloc(stmt: Executable): Double = loc(stmt.body)(1)
  def dloc(stmt: Executable): Double = loc(stmt.body)(0.5)
}