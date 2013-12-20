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
import spoon.reflect.code.CtNewClass
import spoon.reflect.declaration.CtConstructor
import com.sun.corba.se.pept.transport.ContactInfo

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

  def buildTypeHierarchy: Map[CtTypeReference[_], Set[CtClass[_]]] = {
    val supers = scala.collection.mutable.Map[CtSimpleType[_], Set[CtSimpleType[_]]]() withDefaultValue {
      Set()
    }
    val depends = scala.collection.mutable.Map[CtTypeReference[_], Set[CtClass[_]]]() withDefaultValue {
      Set()
    }
    val declared = factory.Class.getAll(true).toSet ++
      factory.all[CtNewClass[_]].map(_.getAnonymousClass).filter(_ != null)
    val refToType: Map[CtTypeReference[_], CtSimpleType[_]] =
      (for (c <- declared) yield (c.getReference, c)).toMap

    // getSupers from CtSimpleType doesn't require reflection
    def getSupers(c: CtSimpleType[_]): Set[CtSimpleType[_]] = {
      def getSuperRefs(c: CtSimpleType[_]) = c match {
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
      getSuperRefs(c).collect {  // filter: types declared only
        case ref if refToType.contains(ref) => refToType(ref)
      }
    }

    // build supers map (restricting declared types only)
    for (c <- declared) {
      supers(c) = getSupers(c)
    }

    // add t(class) to its supertypes(classes/interfaces) dependents list
    def addDependToSupers(t: CtClass[_]) {
      def toSuper(sub: CtSimpleType[_]) {
        supers(sub) foreach { sup =>
          depends(sup.getReference) = depends(sup.getReference) + t
          toSuper(sup)
        }
      }
      toSuper(t)
    }
    // build dependents map
    for {
      c <- declared if c.isInstanceOf[CtClass[_]]
    } addDependToSupers(c.asInstanceOf[CtClass[_]])

    depends.toMap withDefaultValue { Set() }
  }

  def isOverriding(a: CtMethod[_], b: CtExecutableReference[_]): Boolean = {
    a.getSignature == b.getDeclaration.getSignature
  }

  def hasBody[T <: CtExecutable[_]](t: T): Boolean = !t.isImplicit && t.getBody != null

  def invokestatic(in: CtInvocation[_]): Boolean = {
    val ex = in.getExecutable
    ex.isStatic || ex.getSimpleName == "<init>"
  }
  def invokespecial(in: CtInvocation[_]): Boolean = {
    in.getTarget != null && in.getTarget.toString == "super"
  }
  def invokeinterface(in: CtInvocation[_]): Boolean = {
    in.getTarget != null && in.getTarget.getType.isInterface
  }

  lazy val ncallsMap: Map[CtExecutableReference[_], Int] = {
    val depends = buildTypeHierarchy
    def overriding(ref: CtExecutableReference[_]): Set[CtExecutableReference[_]] =
      for {
        t <- depends(ref.getDeclaringType)
        e <- t.getMethods if isOverriding(e, ref)
      } yield e.getReference

    val counter = scala.collection.mutable.Map[CtExecutableReference[_], Int]() withDefaultValue 0

    factory.all[CtAbstractInvocation[_]].foreach {
      case in: CtInvocation[_] =>
        if (invokestatic(in)) { // invokestatic
          counter(in.getExecutable) += 1
        } else if (invokespecial(in)) { // invokespecial
          counter(in.getExecutable) += 1
        } else if (invokeinterface(in)) { // invokeinterface
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