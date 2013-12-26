package com.lge.metr

import scala.collection.JavaConversions._

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner



@RunWith(classOf[JUnitRunner])
class CCCounterTest extends FunSuite with CCCounter {

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

  implicit def strToBlock(body: String) = {
    val f = SpoonLauncher(testSrc(body))
    f.allExecutables(0)
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