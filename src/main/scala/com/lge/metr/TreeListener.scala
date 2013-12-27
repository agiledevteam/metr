package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer

import com.lge.metr.JavaParser.BlockContext
import com.lge.metr.JavaParser.BlockStatementContext
import com.lge.metr.JavaParser.ClassDeclarationContext
import com.lge.metr.JavaParser.ConstructorDeclarationContext
import com.lge.metr.JavaParser.CreatorContext
import com.lge.metr.JavaParser.EnumConstantContext
import com.lge.metr.JavaParser.EnumDeclarationContext
import com.lge.metr.JavaParser.InnerCreatorContext
import com.lge.metr.JavaParser.MethodDeclarationContext
import com.lge.metr.JavaParser.StatementContext

class TreeListener extends JavaBaseListener {
  import JavaModel._
  val executables = ListBuffer[Executable]()

  var scope = List("")
  def pushScope(name: String) { scope = name :: scope }
  def popScope() { scope = scope.tail }
  def typeName = scope.mkString("/")

  override def enterClassDeclaration(ctx: ClassDeclarationContext) { pushScope(ctx.Identifier.getText) }
  override def exitClassDeclaration(ctx: ClassDeclarationContext) { popScope() }
  override def enterCreator(ctx: CreatorContext) { pushScope(ctx.createdName.getText) }
  override def exitCreator(ctx: CreatorContext) { popScope() }
  override def enterInnerCreator(ctx: InnerCreatorContext) { pushScope(ctx.Identifier.getText) }
  override def exitInnerCreator(ctx: InnerCreatorContext) { popScope() }
  override def enterEnumConstant(ctx: EnumConstantContext) { pushScope(ctx.Identifier.getText) }
  override def exitEnumConstant(ctx: EnumConstantContext) { popScope }
  override def enterEnumDeclaration(ctx: EnumDeclarationContext) { pushScope(ctx.Identifier.getText) }
  override def exitEnumDeclaration(ctx: EnumDeclarationContext) { popScope() }

  override def enterMethodDeclaration(ctx: MethodDeclarationContext) {
    if (ctx.methodBody != null) {
      executables += Method(typeName + "." + ctx.Identifier.getText, asBlock(ctx.methodBody.block))
    }
  }
  override def enterConstructorDeclaration(ctx: ConstructorDeclarationContext) {
    executables += Ctor(typeName + ".<init>", asBlock(ctx.constructorBody.block))
  }

  def asBlock(b: BlockContext): BlockStmt = {
    BlockStmt(b.blockStatement.toList.map(asStmt))
  }
  def asStmt(stmt: StatementContext): Stmt =
    stmt.getChild(0).getText match {
      case "if" =>
        if (stmt.statement.size == 1) IfStmt(asStmt(stmt.statement(0)), Nil)
        else IfStmt(asStmt(stmt.statement(0)), List(asStmt(stmt.statement(1))))
      case "for" => LoopStmt("for", asStmt(stmt.statement(0)))
      case "while" => LoopStmt("while", asStmt(stmt.statement(0)))
      case "do" => LoopStmt("do", asStmt(stmt.statement(0)))
      case "try" =>
        TryStmt(asBlock(stmt.block),
          stmt.catchClause.toList.map(c => asBlock(c.block)),
          if (stmt.finallyBlock != null) List(asBlock(stmt.finallyBlock.block)) else Nil)
      case "switch" => SwitchStmt(
        stmt.switchBlockStatementGroup.toList.flatMap(g =>
          g.switchLabel.toList.init.map(_ => BlockStmt(List())) :+
            BlockStmt(g.blockStatement.toList.map(s => asStmt(s)))) ++ stmt.switchLabel.toList.map(_ => BlockStmt(List())))
      case "synchronized" =>
        SyncStmt(asBlock(stmt.block))
      case ";" =>
        BlockStmt(List())
      case _ =>
        if (stmt.block != null) { asBlock(stmt.block) }
        else if (stmt.getChild(1).getText == ":") { asStmt(stmt.statement(0)) }
        else OtherStmt()
    }

  def asStmt(bs: BlockStatementContext): Stmt = {
    if (bs.statement == null) {
      OtherStmt()
    } else {
      asStmt(bs.statement)
    }
  }
}