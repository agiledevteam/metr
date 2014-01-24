package com.lge.metr

object JavaModel {

  case class CompUnit(exes: Seq[Executable])

  abstract class Executable {
    val name: String
    val body: Stmt
  }

  case class Method(name: String, body: Stmt) extends Executable

  case class Ctor(name: String, body: Stmt) extends Executable

  class Stmt

  case class IfStmt(thenPart: Stmt, elsePart: Option[Stmt]) extends Stmt

  // each case has BlockStmt which can be empty 
  case class SwitchStmt(cases: Seq[Stmt]) extends Stmt

  case class LoopStmt(keyword: String, body: Stmt) extends Stmt

  case class BlockStmt(statements: Seq[Stmt]) extends Stmt

  case class SyncStmt(body: Stmt) extends Stmt

  case class TryStmt(body: Stmt, catchers: Seq[Stmt], finalizer: Option[Stmt]) extends Stmt

  case class OtherStmt() extends Stmt
}