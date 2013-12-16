package com.lge.metr

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spoon.support.builder.CtResource
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.declaration.CtInterface
import com.lge.metr.SpoonEx._
import spoon.reflect.code.CtAbstractInvocation

@RunWith(classOf[JUnitRunner])
class SpoonExTest extends FunSuite {
  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile("public class Test{}; \n"+src, "Test.java")
  }

  test("test for factory -> List") {
    val src = """
interface IA {
}
class A implements IA {
}
interface IB extends IA {
}
class B implements IB {
}
      """
    val f = Loader.load(src)
    val ifs = f.all[CtInterface[_]]
    expect(2)(ifs.size)
    assert(ifs.map(_.getSimpleName).contains("IA"))
    assert(ifs.map(_.getSimpleName).contains("IB"))
  }
}