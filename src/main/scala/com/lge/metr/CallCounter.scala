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

  implicit class RichExecutable[T](t: CtExecutable[T]) {
    def isImplementing(ref: CtExecutableReference[_]): Boolean =
      t.getReference.isOverriding(ref) && t.getReference != ref
    def isOverriding(ref: CtExecutableReference[_]): Boolean =
      t.getReference.isOverriding(ref) || t.getReference == ref
    def hasBody: Boolean =
      !t.isImplicit && t.getBody != null
  }

  def ncalls(m: CtExecutable[_]): Int = ncallsMap(nameFor(m))

  lazy val ncallsMap: Map[String, Int] = {
    val map = scala.collection.mutable.Map[String, Int]() withDefaultValue 0
    val methods = factory.all[CtMethod[_]] filter (_.hasBody)
    factory.all[CtAbstractInvocation[_]].foreach {
      case in: CtInvocation[_] =>
        if (in.getTarget == null) { // super() in ctor
          map(nameFor(in.getExecutable)) += 1
        } else if (in.getTarget.toString == "super") { // static
          map(nameFor(in.getExecutable)) += 1
        } else if (in.getTarget.getType.isInterface) {
          methods.filter(_.isImplementing(in.getExecutable)).foreach(m => map(nameFor(m)) += 1)
        } else {
          methods.filter(_.isOverriding(in.getExecutable)).foreach(m => map(nameFor(m)) += 1)
        }
      case ctor =>
        if (ctor.getExecutable != null)
          map(nameFor(ctor.getExecutable)) += 1
    }
    val (sys, non) = factory.all[CtExecutable[_]] filter (_.hasBody) filterNot (map contains nameFor(_)) partition (isOverridingSystem(_))
    sys foreach { m => map(nameFor(m)) = 1 }
    non foreach { m => map(nameFor(m)) = 0 }
    map.toMap
  }
}