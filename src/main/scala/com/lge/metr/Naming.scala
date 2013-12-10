package com.lge.metr
import spoon.reflect.declaration.CtExecutable

trait Naming {
  def nameFor[T](c: CtExecutable[T]) =
    c.getDeclaringType.getQualifiedName + ":" + c.getSimpleName
}
