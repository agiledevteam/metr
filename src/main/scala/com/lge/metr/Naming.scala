package com.lge.metr

import scala.collection.JavaConversions._

trait Naming {
  import JavaModel._

//  def encodeArrayType(t: TypeRef): String = t match {
//    case ArrayTypeRef(n)=> "[" * n
//    case _ => ""
//  }

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
//
//  def nameFor(t: TypeRef): String =
//    encodeArrayType(t) + encodeTypeName(t.qualifiedName)

  def slash(s: String): String = s.replace('.', '/')

//  def nameFor(e: Executable): String = {
//    val methodName = slash(e.declType.qualifiedName) + "." + c.simpleName
//    val param = e.parameterTypes.map(nameFor(_) + ";").mkString
//    val ret = if (e.isConstructor) "V" else nameFor(e.typ)
//    methodName + ":(" + param + ")" + ret
//  }
}
