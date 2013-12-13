package com.lge.metr

import spoon.reflect.Factory
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.filter.AbstractFilter
import spoon.reflect.visitor.Query
import spoon.reflect.visitor.filter.InvocationFilter
import spoon.reflect.declaration.CtType
import spoon.reflect.declaration.CtClass
import scala.collection.JavaConversions._
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.declaration.CtConstructor

trait CallCounter extends Naming {

  class InvokeFilter[T] extends AbstractFilter[CtAbstractInvocation[T]](classOf[CtAbstractInvocation[T]]) {
    override def matches(t: CtAbstractInvocation[T]): Boolean = true
  }

  def allInvokes(m: Factory): List[CtAbstractInvocation[_]] = {
    Query.getElements(m, new InvokeFilter).toList
  }

  implicit class ExecutableWrapper[T](exeRef: CtExecutableReference[T]) {
    def getActual: String =
      if (exeRef.isConstructor())
        exeRef.getActualConstructor.toString
      else
        exeRef.getActualMethod.toString
  }

  def ncalls(f: Factory): Map[String, Int] = {
    allInvokes(f).groupBy(inv => nameFor(inv.getExecutable)).mapValues(_.size)
  }
}