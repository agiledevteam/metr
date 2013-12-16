package com.lge.metr

import scala.collection.JavaConversions.asScalaSet
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass
import spoon.reflect.code.CtBlock

class LocCalculator extends AbstractProcessor[CtClass[_]] with Naming with LocCounter {
  override def process(klass: CtClass[_]) {
    for (c <- klass.getAllMethods ++ klass.getConstructors if !c.isImplicit() && c.getBody != null) {
      println(nameFor(c) + "\t" + sloc(c) + "\t" + dloc(c))
    }
  }
}