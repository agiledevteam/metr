package com.lge.metr

import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.CtVisitor
import spoon.reflect.visitor.CtScanner
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtInterface
import spoon.reflect.code.CtInvocation
import spoon.reflect.declaration.CtExecutable

class ComplexityCalculator extends AbstractProcessor[CtInvocation[_]] {
  val m = scala.collection.mutable.Map[String, Int]()
  def visitor(method: CtMethod[_]) = new CtScanner {
    override def visitCtInvocation[T](invocation: CtInvocation[T]) {
      super.visitCtInvocation(invocation)
    }
  }
  override def processingDone() {
    for ((k,v) <- m) {
      println(s"$k\t$v")
    }
  }
  override def process(invocation: CtInvocation[_]) {
    if (invocation.isImplicit) {
      ()
    } else {
      val exe = invocation.getExecutable
      val methodName = exe.getDeclaringType.toString
      //println(exe)
      m.update(exe.toString, m.getOrElse(exe.toString, 0) + 1)
    }
  }
}