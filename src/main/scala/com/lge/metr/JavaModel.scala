package com.lge.metr

object JavaModel {

  abstract class Executable {
    val name: String
    val body: BlockStmt
  }

  case class Method(name: String, body: BlockStmt) extends Executable

  case class Ctor(name: String, body: BlockStmt) extends Executable

  class Stmt

  case class IfStmt(thenPart: Stmt, elsePart: List[Stmt]) extends Stmt

  case class SwitchStmt(cases: List[Stmt]) extends Stmt

  case class LoopStmt(keyword: String, body: Stmt) extends Stmt

  case class BlockStmt(statements: List[Stmt]) extends Stmt

  case class SyncStmt(body: BlockStmt) extends Stmt

  case class TryStmt(body: BlockStmt, catchers: List[BlockStmt], finalizer: List[BlockStmt]) extends Stmt
  
  case class OtherStmt() extends Stmt
}