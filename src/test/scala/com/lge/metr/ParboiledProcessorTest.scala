package com.lge.metr

import scala.language.implicitConversions
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParboiledProcessorTest extends ProcessorTest {
  val processor = new ParboiledJavaProcessor
}