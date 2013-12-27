package com.lge.metr

object JavaModel {

  abstract class TypeRef {
    val qualifiedName: String
  }

  case class ArrayTypeRef(qualifiedName: String, dimension: Int) extends TypeRef

  case class ScalaTypeRef(qualifiedName: String)

  abstract class Executable {
    val name: String
    val body: BlockStmt
    val declType: TypeRef = null
    val parameterTypes: List[TypeRef] = Nil
    val typ: TypeRef = null
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