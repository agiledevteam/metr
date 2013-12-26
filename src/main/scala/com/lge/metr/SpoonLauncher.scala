package com.lge.metr

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.PrintWriter

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

import com.lge.metr.JavaParser.BlockContext
import com.lge.metr.JavaParser.BlockStatementContext
import com.lge.metr.JavaParser.ClassDeclarationContext
import com.lge.metr.JavaParser.CompilationUnitContext
import com.lge.metr.JavaParser.ConstructorDeclarationContext
import com.lge.metr.JavaParser.CreatorContext
import com.lge.metr.JavaParser.EnumConstantContext
import com.lge.metr.JavaParser.EnumDeclarationContext
import com.lge.metr.JavaParser.InnerCreatorContext
import com.lge.metr.JavaParser.MethodDeclarationContext
import com.lge.metr.JavaParser.StatementContext

import JavaModel.BlockStmt
import JavaModel.Ctor
import JavaModel.Executable
import JavaModel.IfStmt
import JavaModel.LoopStmt
import JavaModel.Method
import JavaModel.OtherStmt
import JavaModel.Stmt
import JavaModel.SwitchStmt
import JavaModel.SyncStmt
import JavaModel.TryStmt

class SpoonLauncher extends LocCounter with Naming {

  import JavaModel._

  abstract class Resource {
    def inputStream: InputStream
  }
  class StringResource(src: String) extends Resource {
    def inputStream = new ByteArrayInputStream(src.getBytes)
  }
  class FileResource(f: File) extends Resource {
    def inputStream = new FileInputStream(f)
  }

  val input = ListBuffer[Resource]()

  def addSource(src: String) {
    input += new StringResource(src)
  }
  def addSource(f: File) {
    if (f.isDirectory) {
      f.listFiles.foreach(addSource(_))
    } else if (f.getPath().endsWith(".java")) {
      input += new FileResource(f)
    }
  }

  def parse(in: InputStream): CompilationUnitContext = {
    val input = new ANTLRInputStream(in);
    val source = new JavaLexer(input);
    val tokens = new CommonTokenStream(source);
    val p = new JavaParser(tokens);
    p.compilationUnit();
  }

  var compilationUnits: List[CompilationUnitContext] = List()

  def load() {
    compilationUnits = input.toList map { i =>
      parse(i.inputStream)
    }
  }

  def generate(reportFile: String) {
    val handlers: List[Executable => Any] = List(sloc(_), dloc(_), m => m.name)
    val p = new PrintWriter(reportFile)
    allExecutables foreach { e =>
      p.println(handlers.map(h => h(e)).mkString("\t"))
    }
    p.close
  }

  def allExecutables: List[Executable] = {
    compilationUnits.flatMap(findExecutableIn _)
  }

  class Listener extends JavaBaseListener {
    val executables = ListBuffer[Executable]()
    var scope = List("")
    override def enterClassDeclaration(ctx: ClassDeclarationContext) {
      scope = ctx.Identifier.getText :: scope
    }
    override def exitClassDeclaration(ctx: ClassDeclarationContext) {
      scope = scope.tail
    }
    override def enterCreator(ctx: CreatorContext) {
      scope = ctx.createdName.getText :: scope
    }
    override def exitCreator(ctx: CreatorContext) {
      scope = scope.tail
    }
    override def enterInnerCreator(ctx: InnerCreatorContext) {
      scope = ctx.Identifier.getText :: scope
    }
    override def exitInnerCreator(ctx: InnerCreatorContext) {
      scope = scope.tail
    }
    override def enterEnumConstant(ctx: EnumConstantContext) {
      scope = ctx.Identifier.getText :: scope
    }
    override def exitEnumConstant(ctx: EnumConstantContext) {
      scope = scope.tail
    }
    override def enterEnumDeclaration(ctx: EnumDeclarationContext) {
      scope = ctx.Identifier.getText :: scope
    }
    override def exitEnumDeclaration(ctx: EnumDeclarationContext) {
      scope = scope.tail
    }
    override def enterMethodDeclaration(ctx: MethodDeclarationContext) {
      if (ctx.methodBody != null) {
        executables += Method(typeName+"."+ctx.Identifier.getText, asBlock(ctx.methodBody.block))
      }
    }
    override def enterConstructorDeclaration(ctx: ConstructorDeclarationContext) {
      executables += Ctor(typeName+".<init>", asBlock(ctx.constructorBody.block))
    }
    def typeName = scope.mkString("/")
    
    def asBlock(b: BlockContext): BlockStmt = {
      BlockStmt(b.blockStatement.toList.map(asStmt))
    }
    def asStmt(stmt: StatementContext): Stmt =
      stmt.getChild(0).getText match {
        case "if" =>
          if (stmt.statement.size == 1) IfStmt(asStmt(stmt.statement(0)), null)
          else IfStmt(asStmt(stmt.statement(0)), asStmt(stmt.statement(1)))
        case "for" => LoopStmt("for", asStmt(stmt.statement(0)))
        case "while" => LoopStmt("while", asStmt(stmt.statement(0)))
        case "do" => LoopStmt("do", asStmt(stmt.statement(0)))
        case "try" =>
          TryStmt(asBlock(stmt.block),
            stmt.catchClause.toList.map(c => asBlock(c.block)),
            if (stmt.finallyBlock != null) asBlock(stmt.finallyBlock.block) else null)
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
  def findExecutableIn(cu: CompilationUnitContext): List[Executable] = {
    val listener = new Listener
    new ParseTreeWalker().walk(listener, cu)
    listener.executables.toList
  }
}

object SpoonLauncher {
  def apply(src: String): SpoonLauncher = {
    val launcher = new SpoonLauncher
    launcher.addSource(src)
    launcher.load
    launcher
  }
  def apply(src: File): SpoonLauncher = {
    val launcher = new SpoonLauncher
    launcher.addSource(src)
    launcher.load
    launcher
  }
}