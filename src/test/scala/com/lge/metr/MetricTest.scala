package com.lge.metr

import scala.language.implicitConversions

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
    val m = new Metric
    val compUnit = m.parse(new StringResource(testSrc(body)).inputStream)
    m.findExecutableIn(compUnit)(0)
  }
}