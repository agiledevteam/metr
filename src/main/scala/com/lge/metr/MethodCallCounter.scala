package com.lge.metr

import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.CtVisitor
import spoon.reflect.visitor.CtScanner
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtInterface
import spoon.reflect.code.CtInvocation
import spoon.reflect.declaration.CtExecutable

class MethodCallCounter extends AbstractProcessor[CtExecutable[_]] with Naming {
  val m = scala.collection.mutable.Map[String, Int]()

  override def processingDone() {
    for ((k, v) <- m) {
      println(s"$k\t$v")
    }
  }

  // handle CtInvocation and CtConstructor
  override def process(exe: CtExecutable[_]) {
    if (!exe.isImplicit) {
      m.update(nameFor(exe), m.getOrElse(exe.toString, 0) + 1)
    }
  }
}