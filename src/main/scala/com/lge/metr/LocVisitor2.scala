package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet

import spoon.reflect.code.CtAssignment
import spoon.reflect.code.CtBreak
import spoon.reflect.code.CtCase
import spoon.reflect.code.CtFor
import spoon.reflect.code.CtForEach
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtInvocation
import spoon.reflect.code.CtLocalVariable
import spoon.reflect.code.CtNewClass
import spoon.reflect.code.CtSwitch
import spoon.reflect.code.CtVariableAccess
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.CtScanner

class LocVisitor2 extends CtScanner {
  var loc = 0

  def onSameLine(a: CtElement, b: CtElement): Boolean = {
    a.getPosition.getLine == b.getPosition.getLine
  }
  override def visitCtVariableAccess[T](variableAccess: CtVariableAccess[T]) {
    //loc += 1
  }
  override def visitCtInvocation[T](invocation: CtInvocation[T]) {
    println("invoke" + invocation.getExecutable().getSimpleName())
    val old = loc
    if (invocation.getTarget() != null) {
      println("target" + invocation.getTarget())
      invocation.getTarget().accept(this)
    }
    invocation.getArguments().foreach(_.accept(this))
    if (loc == old) {
      loc += 1
      println("loc == old")
    }
  }
  override def visitCtLocalVariable[T](localVariable: CtLocalVariable[T]) {
    if (localVariable.getDefaultExpression() != null) {
      localVariable.getDefaultExpression().accept(this)
      if (!onSameLine(localVariable, localVariable.getDefaultExpression()))
        loc += 1
    }

  }
  override def visitCtAssignment[T, R <: T](ass: CtAssignment[T, R]) {
    println("ass=" + ass.getAssigned)
    ass.getAssigned().accept(this)
    ass.getAssignment().accept(this)
  }
  override def visitCtIf(ifElement: CtIf) {
    ifElement.getCondition().accept(this)
    ifElement.getThenStatement().accept(this)
    ifElement.getElseStatement().accept(this)
  }
  override def visitCtNewClass[T](newClass: CtNewClass[T]) {
    println("newclass" + newClass.getExecutable().getSimpleName)
    loc += 1
    newClass.getArguments().foreach(_.accept(this))
    if (newClass.getAnonymousClass() != null) {
      val anony = newClass.getAnonymousClass()
      println(anony.getPosition.getLine)
      anony.accept(this)
    }
  }

  override def visitCtFor(forLoop: CtFor) {
    loc += 1
  }
  override def visitCtForEach(forLoop: CtForEach) {
    loc += 1
  }
  override def visitCtBreak(breakStatement: CtBreak) {
    loc += 1
  }
  override def visitCtMethod[T](m: CtMethod[T]) {
    m.getBody().getStatements().foreach(_.accept(this))
    if (!m.getAnnotations().isEmpty())
      loc += 1
    loc += 1

  }
  override def visitCtClass[T](ctClass: CtClass[T]) {
    ctClass.getMethods().foreach(_.accept(this))
  }
  override def visitCtCase[S](c: CtCase[S]) {
    val stmts = c.getStatements
    stmts.foreach(_.accept(this))
    if (stmts.isEmpty || !onSameLine(c, stmts.head)) // fall-through
      loc += 1
  }
  override def visitCtSwitch[S](c: CtSwitch[S]) {
    //c.getSelector.accept(this)
    loc += 1
    c.getCases.foreach(_.accept(this))
  }

}
