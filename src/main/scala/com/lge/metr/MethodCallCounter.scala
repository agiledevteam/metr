package com.lge.metr
import scala.collection.JavaConversions._

import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.CtVisitor
import spoon.reflect.visitor.CtScanner
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtInterface
import spoon.reflect.code.CtInvocation
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.Query
import spoon.reflect.visitor.Filter
import spoon.reflect.visitor.filter.InvocationFilter
import spoon.reflect.declaration.CtElement
import spoon.reflect.visitor.filter.AbstractFilter

class MethodCallCounter extends AbstractProcessor[CtClass[_]] with Naming {
  val m = scala.collection.mutable.Map[String, Int]()

  override def processingDone() {
    for ((k, v) <- m) {
      println(s"$k\t$v")
    }
  }

  object inv extends AbstractFilter[CtInvocation[_]](classOf[CtInvocation[_]]) {
    override def matches(elem: CtInvocation[_]): Boolean = {
      true
    }
  }
  // handle CtInvocation and CtConstructor
  override def process(klass: CtClass[_]) {
    val invokes = Query.getElements(klass, inv)
    for (i <- invokes if !i.isImplicit) {
      println(nameFor(i))
    }
  }
}