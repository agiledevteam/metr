package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer

import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLoop
import spoon.reflect.code.CtStatement

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

  def dloc(stmt: CtStatement): Double = stmt match {
    case ifStatement: CtIf =>
      ifElseChain(ifStatement).map(then =>
        1 + dloc(then) * 0.5).foldLeft(0.0)(_ + _)
    case blockStmt: CtBlock[_] =>
      blockStmt.getStatements.map(s =>
        dloc(s)).foldLeft(0.0)(_ + _)
    case loopStmt: CtLoop =>
      0
    case s =>
      plainLoc(s.toString)
  }
}