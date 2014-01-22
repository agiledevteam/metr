package com.lge.metr

object JavaModel {

  case class CompUnit(exes: Seq[Executable])

  abstract class Executable {
    val name: String
    val body: BlockStmt
  }

  case class Method(name: String, body: BlockStmt) extends Executable

  case class Ctor(name: String, body: BlockStmt) extends Executable

  class Stmt

  case class IfStmt(thenPart: Stmt, elsePart: Option[Stmt]) extends Stmt

  case class SwitchStmt(cases: Seq[Stmt]) extends Stmt

  case class LoopStmt(keyword: String, body: Stmt) extends Stmt

  case class BlockStmt(statements: Seq[Stmt]) extends Stmt

  case class SyncStmt(body: BlockStmt) extends Stmt

  case class TryStmt(body: BlockStmt, catchers: Seq[BlockStmt], finalizer: Option[BlockStmt]) extends Stmt

  case class OtherStmt() extends Stmt
}