package com.lge.metr

import scala.collection.JavaConversions.asScalaSet
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.declaration.CtExecutable

class LocCalculator extends AbstractProcessor[CtClass[_]] {

  def blankLine(l: String) = l forall (!_.isLetterOrDigit)

  def plainLoc(s: String) = (s.lines filterNot blankLine).size

  def nameFor[T](c: CtExecutable[T]) =
    c.getDeclaringType.getQualifiedName + ":" + c.getSimpleName

  override def process(klass: CtClass[_]) {
    for (c <- klass.getAllMethods ++ klass.getConstructors if !c.isImplicit()) {
      println(nameFor(c) + ":" + plainLoc(c.getBody.toString))
    }
  }
}