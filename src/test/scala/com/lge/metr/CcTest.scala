package com.lge.metr

import scala.language.implicitConversions

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CcTest extends FunSuite with MetricCounter with MetricTest{



  test("straight forward  cc") {
    val body = """
      int a;
      return;
      """
    expectResult(1)(cc(body))
  }

  test("if-else cc") {
    val body = """
      if (true) {
        int a = 0;
        a++;
      }
      return;
      """
    expectResult(2)(cc(body))
  }

}