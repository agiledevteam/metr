package com.lge.metr

import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.code.CtIf

trait CCCounter {

  def cc(stmt: CtStatement): Int = {
    stmt match {
      case null => 0
      case _: CtIf => 2
      case _ => 1
    }

  }
}