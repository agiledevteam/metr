package com.lge.metr

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import spoon.support.builder.CtResource
import spoon.support.builder.support.CtVirtualFile

@RunWith(classOf[JUnitRunner])
class CallCountTest extends FunSuite with CallCounter {

  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile("public class Test{}; \n" + src, "Test.java")
  }

  test("invokevirtual - direct calls") {
    val src = """
class A {
  int a;
  B b;
  A(B b) {
     a = 0;
     this.b = b;
  }
      
  void f() {
     b.g();
     b.g();
  }
  public static void main(String args[]) {
     A a = new A(new B());
     a.f();
  }
}	
class B {
  void g() {
    ;
  }
}
      """
    val f = Loader.load(src)
    val m = ncalls(f)
    expect(1)(m("A.f:()V"))
    expect(2)(m("B.g:()V"))
    expect(1)(m("A.<init>:(LB;)V"))
  }

}