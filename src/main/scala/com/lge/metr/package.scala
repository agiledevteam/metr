package com.lge

import scala.collection.JavaConversions.asScalaBuffer

import com.lge.metr.Naming

import spoon.reflect.Factory
import spoon.reflect.declaration.CtElement
import spoon.reflect.visitor.Query
import spoon.reflect.visitor.filter.AbstractFilter

package object metr extends Naming {
  class AllFilter[T <: CtElement](cls: Class[T]) extends AbstractFilter[T](cls) {
    override def matches(e: T): Boolean = true
  }

  implicit class FactoryOps(val f: Factory) extends AnyVal {
    def all[T <: CtElement: Manifest]: List[T] =
      Query.getElements(f, new AllFilter[T](manifest[T].runtimeClass.asInstanceOf[Class[T]])).toList
  }

}