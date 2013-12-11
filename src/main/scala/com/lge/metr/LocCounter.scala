package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLoop
import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtDo
import spoon.reflect.code.CtSynchronized
import spoon.reflect.code.CtTry
import spoon.reflect.code.CtSwitch

trait LocCounter {

  def blankLine(l: String) = l forall (!_.isLetterOrDigit)

  def plainLoc(s: String) = (s.lines filterNot blankLine).size

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

  def dloc(stmts: Iterable[CtStatement]): Double =
    stmts.map(dloc(_)).foldLeft(0.0)(_ + _)

  def dloc(stmt: CtStatement): Double = stmt match {
    case null =>
      0
    case ifStatement: CtIf =>
      ifElseChain(ifStatement).map(then =>
        1 + dloc(then) * 0.5).foldLeft(0.0)(_ + _)
    case blockStmt: CtBlock[_] =>
      dloc(blockStmt.getStatements.toIterable)
    case loopStmt: CtLoop =>
      loopConditionLoc(loopStmt) + dloc(loopStmt.getBody) * 0.5
    case syncStmt: CtSynchronized =>
      1 + dloc(syncStmt.getBlock)
    case tryStmt: CtTry =>
      val loc = tryStmt.getCatchers.map(c =>
        dloc(c.getBody) + 1).foldLeft(dloc(tryStmt.getBody) + 1)(_ + _)
      if (tryStmt.getFinalizer != null)
        loc + dloc(tryStmt.getFinalizer) + 1
      else
        loc
    case switchStmt: CtSwitch[_] =>
      switchStmt.getCases.map(c => dloc(c.getStatements) * 0.5 + 1).foldLeft(1.0)(_ + _)
    case s =>
      plainLoc(s.toString)
  }
}