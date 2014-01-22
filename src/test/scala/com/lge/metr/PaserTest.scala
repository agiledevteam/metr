package com.lge.metr

import scala.language.implicitConversions
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.lge.metr.JavaModel._

@RunWith(classOf[JUnitRunner])
class ParserTest extends FunSuite {
  test("parse input") {
    val p = new ParboiledJavaProcessor
    val cu = p.process("""
class Test {
  void f() {
    int a = 3;
    int b = 3;
  }
}
        """)
    expectResult(CompUnit(List(Method("f", BlockStmt(List())))))(cu)
  }
}