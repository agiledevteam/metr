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

  lazy val ncallsMap: Map[String, Int] = {

    def hasBody[T <: CtExecutable[_]](t: T): Boolean = !t.isImplicit && t.getBody != null

    val methods = factory.all[CtMethod[_]] filter (hasBody)

    val overridingMethods: scala.collection.mutable.Map[String, List[CtExecutableReference[T] forSome { type T }]] =
      scala.collection.mutable.Map[String, List[CtExecutableReference[T] forSome { type T }]]()

    def getOverridings(ref: CtExecutableReference[T] forSome { type T}) = {
      val name = nameFor(ref)
      if (!overridingMethods.contains(name)) {
        val result = for { 
          m <- methods if m.getReference.isOverriding(ref) && m.getReference != ref
        } yield m.getReference
        overridingMethods(name) = result 
        result
      } else {
        overridingMethods(name)
      }
    }

    def implementing(ref: CtExecutableReference[T] forSome { type T }) = 
      getOverridings(ref)

    def overriding(ref: CtExecutableReference[T] forSome { type T }) = 
      ref :: getOverridings(ref)

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
          implementing(in.getExecutable).foreach(inc(_))
        } else {
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