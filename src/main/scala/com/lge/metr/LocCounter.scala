package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtCase
import spoon.reflect.code.CtDo
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLoop
import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtSwitch
import spoon.reflect.code.CtSynchronized
import spoon.reflect.code.CtTry
import spoon.reflect.declaration.CtExecutable

trait LocCounter {

  def ifElseChain(ifStm: CtIf): List[CtStatement] = {
    var cur: CtStatement = ifStm
    val list = ListBuffer[CtStatement]()
    while (cur != null) {
      cur match {
        case ifS: CtIf =>
          list.add(ifS.getThenStatement); cur = ifS.getElseStatement
        case _ => list.add(cur); cur = null
      }
    }
    list.toList
  }

  def loopConditionLoc(loopStmt: CtLoop): Int = loopStmt match {
    case _: CtDo => 2
    case _ => 1
  }

  implicit def exeToBlock[T, B <: T](m: CtExecutable[T]): CtBlock[B] = m.getBody[B]

  def dloc(stmt: CtStatement)(implicit df: Double = 0.5): Double = stmt match {
    case null =>
      0
    case ifStmt: CtIf =>
      ifElseChain(ifStmt).map(thenPart => 1 + dloc(thenPart) * df).foldLeft(0.0)(_ + _)
    case switchStmt: CtSwitch[_] =>
      switchStmt.getCases.map(c => dloc(c) * df + 1).foldLeft(1.0)(_ + _)
    case loopStmt: CtLoop =>
      loopConditionLoc(loopStmt) + dloc(loopStmt.getBody) * df
    case blockStmt: CtBlock[_] =>
      blockStmt.getStatements.filterNot(_.isImplicit).map(dloc(_)).foldLeft(0.0)(_ + _)
    case caseStmt: CtCase[_] =>
      caseStmt.getStatements.filterNot(_.isImplicit).map(dloc(_)).foldLeft(0.0)(_ + _)
    case syncStmt: CtSynchronized =>
      1 + dloc(syncStmt.getBlock)
    case tryStmt: CtTry =>
      val loc = tryStmt.getCatchers.map(c =>
        dloc(c.getBody) + 1).foldLeft(dloc(tryStmt.getBody) + 1)(_ + _)
      if (tryStmt.getFinalizer != null)
        loc + dloc(tryStmt.getFinalizer) + 1
      else
        loc
    case s =>
      1
  }

  def sloc(stmt: CtStatement): Double = dloc(stmt)(1)
}