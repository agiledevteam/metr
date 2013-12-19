package com.lge.metr

import scala.collection.JavaConversions._
import scala.collection.JavaConversions._
import scala.util.Success
import scala.util.Try
import spoon.reflect.Factory
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.code.CtInvocation
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.declaration.CtInterface
import spoon.reflect.declaration.CtMethod
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtTypeReference
import scala.collection.mutable.ListBuffer

trait CallCounter extends Naming {

  val factory: Factory

  def isSystem[T](t: CtTypeReference[T]): Boolean = {
    val f = t.getFactory
    val p = t.getPackage
    p != null && !f.Package.getAllRoots.exists(_ == p)
  }

  def isOverridingSystem[T](method: CtExecutable[T]): Boolean = {
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

  implicit class RichType[T](t: CtTypeReference[T]) {
    def isInterface: Boolean = t.getDeclaration match {
      case _: CtInterface[_] => true
      case _ => false
    }
  }

  def ncalls(m: CtExecutable[_]): Int = ncallsMap(nameFor(m))

  def getSupers(t: CtTypeReference[_]): List[CtTypeReference[_]] =
    if (t.getSuperclass != null)
      t.getSuperclass :: t.getSuperInterfaces.toList
    else
      t.getSuperInterfaces.toList

  def buildTypeHierarchy: Map[CtTypeReference[_], Set[CtTypeReference[_]]] = {
    val depends = scala.collection.mutable.Map[CtTypeReference[_], Set[CtTypeReference[_]]]() withDefaultValue {
      Set()
    }
    def addDependToSupers(t: CtTypeReference[_]) {
      def toSuper(sub: CtTypeReference[_]) {
        getSupers(sub).foreach { sup =>
          depends(sup) = depends(sup) + t
          toSuper(sup)
        }
      }
      toSuper(t)
    }
    for {
      c <- factory.Class.getAll if !c.getReference.isInterface
    } addDependToSupers(c.getReference)
    
    depends.toMap withDefaultValue { Set() }
  }

  def hasBody[T <: CtExecutable[_]](t: T): Boolean = !t.isImplicit && t.getBody != null

  lazy val ncallsMap: Map[String, Int] = {
    val depends = buildTypeHierarchy

    val methods = factory.all[CtMethod[_]] filter (hasBody)

    def overriding(ref: CtExecutableReference[T] forSome { type T }): Set[CtExecutableReference[_]] = {
      for {
        t <- depends(ref.getDeclaringType)
        e <- t.getDeclaredExecutables if e.isOverriding(ref)
      } yield e
    }

    val counter = scala.collection.mutable.Map[String, Int]() withDefaultValue 0
    def inc(inv: CtExecutableReference[T] forSome { type T }): Unit =
      counter(nameFor(inv)) += 1

    factory.all[CtAbstractInvocation[_]].foreach {
      case in: CtInvocation[_] =>
        if (in.getTarget == null) { // super() in ctor
          inc(in.getExecutable)
        } else if (in.getTarget.toString == "super") { // static
          inc(in.getExecutable)
        } else if (in.getTarget.getType.isInterface) {
          overriding(in.getExecutable).foreach(inc(_))
        } else {
          inc(in.getExecutable)
          overriding(in.getExecutable).foreach(inc(_))
        }
      case ctor =>
        if (ctor.getExecutable != null)
          inc(ctor.getExecutable)
    }
    val (sys, non) = factory.all[CtExecutable[_]] filter (hasBody) filterNot (counter contains nameFor(_)) partition (isOverridingSystem(_))
    sys foreach { m => counter(nameFor(m)) = 1 }
    non foreach { m => counter(nameFor(m)) = 0 }
    counter.toMap
  }

}