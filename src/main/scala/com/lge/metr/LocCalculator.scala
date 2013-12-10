package com.lge.metr

import scala.collection.JavaConversions.asScalaSet

import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass

class LocCalculator extends AbstractProcessor[CtClass[_]] with Naming {

  def blankLine(l: String) = l forall (!_.isLetterOrDigit)

  def plainLoc(s: String) = (s.lines filterNot blankLine).size

  override def process(klass: CtClass[_]) {
    for (c <- klass.getAllMethods ++ klass.getConstructors if !c.isImplicit()) {
      println(nameFor(c) + ":" + plainLoc(c.getBody.toString))
    }
  }
}