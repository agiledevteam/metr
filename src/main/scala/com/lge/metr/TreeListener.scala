package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

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
      executables += Method(typeName+"."+ctx.Identifier.getText, ctx.methodBody.block)
    }
  }
  override def enterConstructorDeclaration(ctx: ConstructorDeclarationContext) {
    executables += Ctor(typeName+".<init>", ctx.constructorBody.block)
  }

  implicit def asBlock(b: BlockContext): BlockStmt = {
    BlockStmt(b.blockStatement.map(asStmt))
  }
  implicit def asStmt(stmt: StatementContext): Stmt = stmt.getChild(0).getText match {
    case "if" =>
      IfStmt(stmt.statement(0), // then part
        stmt.statement().lift(1).map(asStmt)) // else part is optional
    case kw @ ("for" | "while" | "do") =>
      LoopStmt(kw, stmt.statement(0))
    case "try" =>
      TryStmt(stmt.block,
        stmt.catchClause.map(c => asBlock(c.block)),
        Option(stmt.finallyBlock).map(_.block))
    case "switch" =>
      SwitchStmt(stmt.switchBlockStatementGroup.flatMap(g => // each group has
        g.switchLabel.init.map(_ => BlockStmt(List())) :+ // leading empty cases
          BlockStmt(g.blockStatement.map(asStmt))) ++ // case with statements
        stmt.switchLabel.map(_ => BlockStmt(List()))) // trailing empty cases
    case "synchronized" =>
      SyncStmt(stmt.block)
    case ";" =>
      BlockStmt(List())
    case _ =>
      if (stmt.block != null) stmt.block
      else if (stmt.getChild(1).getText == ":") stmt.statement(0)
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