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
import spoon.reflect.code.CtInvocation
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

trait CallCounter extends Naming {

  class InvokeFilter[T](ref: CtExecutableReference[T], includeSuper: Boolean)
    extends AbstractFilter[CtAbstractInvocation[T]](classOf[CtAbstractInvocation[T]]) {
    override def matches(t: CtAbstractInvocation[T]): Boolean = {
      if (includeSuper)
        t.getExecutable() == ref
      else
        t match {
          case in: CtInvocation[T] =>
            t.getExecutable == ref && in.getTarget.toString != "super"
          case ctor =>
            t.getExecutable() == ref
        }
    }
  }

  //
  //  def forEachInvokes[T](m: Factory)(f: CtAbstractInvocation[_] => Unit) {
  //    Query.getElements(m, new InvokeFilter[T](inv => { f(inv); false }))
  //  }
  //  
  def forEachExecutables[T](m: Factory)(handler: CtExecutable[_] => Unit) {
    Query.getElements(m, new AbstractFilter[CtExecutable[T]](classOf[CtExecutable[T]]) {
      def matches(m: CtExecutable[T]): Boolean = {
        handler(m)
        false
      }
    })
  }

  implicit class ExecutableWrapper[T](exeRef: CtExecutableReference[T]) {
    def getActual: String =
      if (exeRef.isConstructor())
        exeRef.getActualConstructor.toString
      else
        exeRef.getActualMethod.toString
  }

  def overriddensOf(m: CtMethod[_]): List[CtExecutableReference[_]] = {
    def loop(ref: CtExecutableReference[_]): List[CtExecutableReference[_]] = {
      if (ref == null) List()
      else ref :: loop(ref.getOverridingExecutable)
    }
    loop(m.getReference)
  }

  def allInvokesTo[T](f: Factory, ref: CtExecutableReference[T], includeSuper: Boolean = true) =
    Query.getElements(f, new InvokeFilter(ref, includeSuper)).toList

  def allInvokes[T](f: Factory, m: CtExecutable[T]): Int = m match {
    case method: CtMethod[T] =>
      overriddensOf(method).foldLeft(0)((acc, ref) => acc + allInvokesTo(f, ref, m.getReference == ref).size)
    case ctor: CtConstructor[T] => allInvokesTo(f, ctor.getReference).size
  }

  def ncalls(f: Factory): Map[String, Int] = {
    val map = scala.collection.mutable.Map[String, Int]()
    forEachExecutables(f) { m =>
      map += (nameFor(m) -> allInvokes(f, m))
    }
    map.toMap
  }
}