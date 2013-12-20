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
import spoon.reflect.declaration.CtSimpleType
import spoon.reflect.declaration.CtClass

trait CallCounter {

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

  def ncalls(m: CtExecutable[_]): Int = ncallsMap(m.getReference)

  def buildTypeHierarchy: Map[CtTypeReference[_], Set[CtTypeReference[_]]] = {

    val supers, depends = scala.collection.mutable.Map[CtTypeReference[_], Set[CtTypeReference[_]]]() withDefaultValue {
      Set()
    }

    val declared = factory.Class.getAll(true).toList
    val declaredRefs = declared.map(_.getReference)

    // getSupers from CtSimpleType doesn't require reflection
    def getSupers(c: CtSimpleType[_]): Set[CtTypeReference[_]] = c match {
      case cls: CtClass[_] =>
        if (cls.getSuperclass == null)
          cls.getSuperInterfaces.toSet
        else
          cls.getSuperInterfaces.toSet + cls.getSuperclass
      case iface: CtInterface[_] =>
        iface.getSuperInterfaces.toSet
      case _ =>
        Set()
    }

    def addDependToSupers(t: CtTypeReference[_]) {
      def toSuper(sub: CtTypeReference[_]) {
        supers(sub) foreach { sup =>
          depends(sup) = depends(sup) + t
          toSuper(sup)
        }
      }
      toSuper(t)
    }

    // build supers map (restricting declared types only)
    for {
      c <- declared
    } supers(c.getReference) = getSupers(c).filter(declaredRefs.contains(_))

    // build dependents map
    for {
      c <- declaredRefs if !c.isInterface
    } addDependToSupers(c)

    depends.toMap withDefaultValue { Set() }
  }

  def hasBody[T <: CtExecutable[_]](t: T): Boolean = !t.isImplicit && t.getBody != null

  lazy val ncallsMap: Map[CtExecutableReference[_], Int] = {
    val depends = buildTypeHierarchy
    def overriding(ref: CtExecutableReference[_]): Set[CtExecutableReference[_]] =
      for {
        t <- depends(ref.getDeclaringType)
        e <- t.getDeclaredExecutables if e.isOverriding(ref)
      } yield e

    val counter = scala.collection.mutable.Map[CtExecutableReference[_], Int]() withDefaultValue 0

    factory.all[CtAbstractInvocation[_]].foreach {
      case in: CtInvocation[_] =>
        if (in.getTarget == null) { // invokestatic
          counter(in.getExecutable) += 1
        } else if (in.getTarget.toString == "super") { // invokespecial
          counter(in.getExecutable) += 1
        } else if (in.getTarget.getType.isInterface) { // invokeinterface
          overriding(in.getExecutable).foreach(counter(_) += 1)
        } else { // invokedynamic
          counter(in.getExecutable) += 1
          overriding(in.getExecutable).foreach(counter(_) += 1)
        }
      case ctor =>
        if (ctor.getExecutable != null)
          counter(ctor.getExecutable) += 1
    }
    val allExe = factory.all[CtExecutable[_]] filter (hasBody)
    val notCalled = allExe filterNot (e => counter.contains(e.getReference))
    notCalled foreach { e =>
      counter(e.getReference) = if (isOverridingSystem(e)) 1 else 0
    }
    counter.toMap
  }

}