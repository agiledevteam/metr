package com.lge.metr

import scala.collection.JavaConversions._

trait CCCounter {
  import JavaModel._
  def cc(exe: Executable): Int = cc(exe.body) + 1

  private def cc(stmt: Stmt): Int = stmt match {
    case IfStmt(t, f) => cc(t) + cc(f) + 1
    case SwitchStmt(cases) => cases.map(cc(_) + 1).sum
    case LoopStmt(_, body) => cc(body) + 1
    case BlockStmt(statements) => statements.map(cc(_)).sum
    case SyncStmt(body) => cc(body)
    case TryStmt(body, catchers, finalizer) => cc(body) + catchers.map(cc(_)).sum + cc(finalizer)
    case _ => 0
  }
}