package com.lge.metr

import scala.collection.JavaConversions._
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spoon.AbstractLauncher
import spoon.reflect.declaration.CtMethod
import spoon.support.builder.CtResource
import spoon.reflect.declaration.CtPackage
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.visitor.Query
import spoon.reflect.Factory
import spoon.reflect.visitor.Filter
import spoon.reflect.visitor.filter.AbstractFilter
import spoon.support.builder.CtFile
import spoon.support.builder.support.CtFileFile
import java.io.File
import scala.io.Source
import spoon.reflect.code.CtBlock
import spoon.reflect.declaration.CtExecutable



@RunWith(classOf[JUnitRunner])
class CCCounterTest extends FunSuite with CCCounter {


  def stringResource(src: String): CtFile =
    new CtVirtualFile(src, "Loc.java")

  def fileResource(path: String): CtFile =
    new CtFileFile(new File(path))

  class MethodFilter[T](name: String) extends AbstractFilter[CtExecutable[T]](classOf[CtExecutable[T]]) {
    override def matches(m: CtExecutable[T]): Boolean = {
      m.getSimpleName == name
    }
  }

  def methodNamed[T](f: Factory, name: String): CtExecutable[T] = {
    Query.getElements(f, new MethodFilter[T](name)).head
  }

  def testSrc(src: String): String = {
    val header =
      """class Cc {
        | public void cc() {
        """.stripMargin
    val footer =
      """
        | }
        |}""".stripMargin
    header + src + footer
  }

  implicit def strToBlock[T](body: String): CtExecutable[T] = {
    val f = Loader.load(stringResource(testSrc(body)))
    methodNamed[T](f, "cc")
  
  }


  test("straight forward  cc") {
    val body = """
      int a;
      return;
      """
   expect(1)(cc(body))
  }
  
   test("if-else cc") {
    val body = """
      if (true) {
        int a = 0;
        a++;
      }
      return;
      """
    expect(2)(cc(body))
  }
  
}