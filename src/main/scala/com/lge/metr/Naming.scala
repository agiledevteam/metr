package com.lge.metr
import spoon.reflect.declaration.CtExecutable
import spoon.reflect.code.CtInvocation
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.reference.CtTypeReference
import scala.collection.JavaConversions._
import spoon.reflect.reference.CtArrayTypeReference

trait Naming {
  def nameFor[T](c: CtExecutable[T]): String =
    nameFor(c.getReference)

  def encodeArrayType[T](c: CtTypeReference[T]): String = c match {
    case ac: CtArrayTypeReference[T] => "[" * ac.getDimensionCount()
    case _ => ""
  }

  def encodeTypeName(c: String): String = c match {
    case "void" => "V"
    case "byte" => "B"
    case "char" => "C"
    case "double" => "D"
    case "float" => "F"
    case "int" => "I"
    case "long" => "J"
    case "short" => "S"
    case "boolean" => "Z"
    case z => "L" + slash(z)
  }

  def nameFor[T](c: CtTypeReference[T]): String =
    encodeArrayType(c) + encodeTypeName(c.getQualifiedName)

  def slash(s: String): String = s.replace('.', '/')

  def nameFor[T](c: CtExecutableReference[T]): String = {
    val methodName = slash(c.getDeclaringType.getQualifiedName) + "." + c.getSimpleName
    val param = c.getParameterTypes.map(nameFor(_) + ";").mkString
    val ret = if (c.isConstructor) "V" else nameFor(c.getType)
    methodName + ":(" + param + ")" + ret
  }
}
