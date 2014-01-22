package com.lge.metr

import scala.language.implicitConversions
import java.io.ByteArrayInputStream

trait MetricTest {
  def testSrc(body: String): String = {
    val header =
      """class Cc {
        | public void cc() {
        """.stripMargin
    val footer =
      """
        | }
        |}""".stripMargin
    header + body + footer
  }

  implicit def strToBlock(body: String) = {
    val m = new AntlrJavaProcessor
    m.process(new ByteArrayInputStream(testSrc(body).getBytes)).exes(0)
  }
}