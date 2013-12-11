package com.lge.metr

import scala.collection.JavaConversions._
import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtStatement
import spoon.reflect.visitor.CtScanner
import scala.collection.mutable.ListBuffer

class DLOCVisitor(w: Double) extends CtScanner with LocCounter {
  var dloc: Double = 0.0
  
  def ifElseChain(ifStm: CtIf): List[CtStatement] = {
    var cur: CtStatement = ifStm
    val list = ListBuffer[CtStatement]()
    while (cur != null) {
      cur match {
        case ifS: CtIf => list.add(ifS.getThenStatement); cur = ifS.getElseStatement
        case _ => list.add(cur); cur = null
      }
    }
    list.toList
  }

  override def visitCtBlock[R](block: CtBlock[R]) {
    dloc += block.getStatements.collect {
      
      case ifStatement: CtIf =>
        val thenList: List[CtStatement] = ifElseChain(ifStatement)
        val loc = thenList.map(then =>
          1 + DLOCVisitor(then, 0.5)
        ).foldLeft(0.0)(_+_)
        println(ifStatement + ":" + loc)
        loc
      case s => 
        plainLoc(s.toString)
      
    }.foldLeft(0.0)(_+_) * w
  }
}

object DLOCVisitor {
  def apply(st: CtStatement, w: Double) : Double = {
    val v = new DLOCVisitor(w)
    st.accept(v)
    println(st + ":" + v.dloc)
    v.dloc
  }
}
