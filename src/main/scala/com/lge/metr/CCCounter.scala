package com.lge.metr

import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtLoop
import spoon.reflect.code.CtSynchronized
import spoon.reflect.code.CtSwitch
import spoon.reflect.code.CtTry
import spoon.reflect.code.CtCase
import scala.collection.JavaConversions._

trait CCCounter {

  def cc[T](stmt: CtExecutable[T]): Int = cc(stmt.getBody()) + 1

  private def cc(stmt: CtStatement): Int = stmt match {
    case null => 0
    case ifStmt: CtIf => cc(ifStmt.getThenStatement()) + cc(ifStmt.getElseStatement()) + 1
    case switchStmt: CtSwitch[_] => switchStmt.getCases.map(cc(_) + 1).sum
    case loopStmt: CtLoop => cc(loopStmt.getBody) + 1
    case blockStmt: CtBlock[_] => blockStmt.getStatements.map(cc(_)).sum
    case caseStmt: CtCase[_] => caseStmt.getStatements.map(cc(_)).sum
    case syncStmt: CtSynchronized => cc(syncStmt.getBlock)
    case tryStmt: CtTry => cc(tryStmt.getBody) + tryStmt.getCatchers.map(c => cc(c.getBody)).sum + cc(tryStmt.getFinalizer)
    case s => 0

  }
}