package com.lge.metr

import scala.collection.JavaConversions._

trait CCCounter {
  import JavaModel._

  def cc(exe: Executable): Int = cc(exe.body) + 1

  private def cc(stmt: Stmt): Int = stmt match {
    case IfStmt(t, f) => 1 + (t :: f).map(cc(_)).sum
    case SwitchStmt(cases) => cases.map(cc(_) + 1).sum
    case LoopStmt(_, body) => cc(body) + 1
    case BlockStmt(statements) => statements.map(cc(_)).sum
    case SyncStmt(body) => cc(body)
    case TryStmt(body, catchers, finalizer) =>
      (body :: catchers ::: finalizer).map(cc(_)).sum
    case _ => 0
  }
}