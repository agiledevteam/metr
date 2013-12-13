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
     b.g();  // invokevirtual B.g -> +1 for overridings
     b.g();
     b.g();
	 b.g();
  }
  public static void main(String args[]) {
     A a = new A(new B());
     a.f();
     new C().g();
     new C().g();
  }
}
class Base {
  void g() {
  }
}
class B extends Base {
  @Override
  void g() {
    ;
  }
}
class C extends B {
  @Override
  void g() { // overrides B.g()
     super.g(); // calls B.g() invokespecial
  }
}
      """
    val f = Loader.load(src)
    val m = ncalls(f)
    expect(1)(m("A.f:()V"))
    expect(5)(m("B.g:()V"))
    expect(6)(m("C.g:()V"))
    expect(1)(m("A.<init>:(LB;)V"))
  }

}