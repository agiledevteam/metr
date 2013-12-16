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
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import spoon.reflect.reference.CtTypeReference
import spoon.reflect.declaration.CtInterface
import spoon.reflect.code.CtNewClass

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
    def loop(refTry: Try[CtExecutableReference[_]]): List[CtExecutableReference[_]] = refTry match {
      case Success(ref) if ref != null =>
        ref :: loop(Try(ref.getOverridingExecutable))
      case _ =>
        List()
    }
    loop(Try(m.getReference))
  }

  def allInvokesTo[T](ref: CtExecutableReference[T], includeSuper: Boolean = true) =
    Query.getElements(ref.getFactory, new InvokeFilter(ref, includeSuper)).toList

  def allInvokes(f: Factory) =
    Query.getElements(f, new AbstractFilter[CtAbstractInvocation[_]](classOf[CtAbstractInvocation[_]]) {
      override def matches(elem: CtAbstractInvocation[_]): Boolean = true
    }).toList

  def allMethods(f: Factory) =
    Query.getElements(f, new AbstractFilter[CtMethod[_]](classOf[CtMethod[_]]) {
      override def matches(elem: CtMethod[_]): Boolean = true
    }).toList

  def isSystem[T](t: CtTypeReference[T]): Boolean = {
    val f = t.getFactory
    val p = t.getPackage
    p != null && !f.Package.getAllRoots.exists(_ == p)
  }

  def isOverridingSystem[T](method: CtMethod[T]): Boolean = {
    def loop(refTry: Try[CtExecutableReference[_]]): Boolean = refTry match {
      case Success(ref) =>
        if (ref == null)
          false
        else if (isSystem(ref.getDeclaringType))
          true
        else
          loop(Try(ref.getOverridingExecutable))
      case _ =>
        true
    }
    loop(Try(method.getReference))
  }

  def ncalls[T](exe: CtExecutable[T]): Int = exe match {
    case method: CtMethod[T] =>
      if (isOverridingSystem(method))
        1
      else
        overriddensOf(method).foldLeft(0)((acc, ref) => acc + allInvokesTo(ref, exe.getReference == ref).size)
    case ctor: CtConstructor[T] =>
      allInvokesTo(ctor.getReference).size
  }

  implicit class RichType[T](t: CtTypeReference[T]) {
    def isInterface: Boolean = t.getDeclaration match {
      case _: CtInterface[_] => true
      case _ => false
    }
  }

  implicit class RichMethod[T](t: CtMethod[T]) {
    def isImplementing(ref: CtExecutableReference[_]): Boolean = true
  }

  def ncalls(f: Factory): Map[String, Int] = {
    val map = scala.collection.mutable.Map[String, Int]() withDefaultValue 0
    allInvokes(f).foreach {
      case in: CtInvocation[_] =>
        if (in.getTarget == null) { // super() in ctor
          map(nameFor(in.getExecutable)) += 1
        } else if (in.getTarget.toString == "super") { // static
          map(nameFor(in.getExecutable)) += 1
        } else if (in.getTarget.getType.isInterface) {
          allMethods(f).filter(_.isImplementing(in.getExecutable)).foreach(m => map(nameFor(m)) += 1)
        } else {

        }
      case ctor =>
        if (ctor.getExecutable != null)
          map(nameFor(ctor.getExecutable)) += 1
    }

    map.toMap
  }
}