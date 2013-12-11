package com.lge.metr
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.code.CtInvocation
import spoon.reflect.reference.CtExecutableReference

trait Naming {
  def nameFor[T](c: CtExecutable[T]):String =
    c.getDeclaringType.getQualifiedName + ":" + c.getSimpleName

  def nameFor[T](c: CtInvocation[T]):String =
    nameFor(c.getExecutable)

  def nameFor[T](c: CtExecutableReference[T]):String =
    c.getDeclaringType.getQualifiedName + ":" + c.getSimpleName
}
