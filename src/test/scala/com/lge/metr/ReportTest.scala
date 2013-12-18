package com.lge.metr

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.ListBuffer

@RunWith(classOf[JUnitRunner])
class ReportTest extends FunSuite {
  test("report depends to sloc/dloc/ncalls") {
    val generated = ListBuffer[Task]()
    val report, sloc, dloc, ncalls = Task("_") { self =>
      generated += self
    }
    assert(report != sloc)

    List(sloc, dloc, ncalls).foreach(report.dependOn(_))
    report.generate
    expect(List(sloc, dloc, ncalls, report))(generated)
  }
}