package com.lge.metr

import scala.language.implicitConversions

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CallCountTest extends FunSuite {

  val src = """
class A {
  int a;
  B b;
  A(B b) {
     a = 0;
     this.b = b;
  }
      
  void f() { // B.g 4, C.g 4
     b.g();
     b.g();
     b.g();
     b.g();
  }
  static void g(IB b) { // C.g 2
     b.g();
     b.g();
  }
  public static void main(String args[]) {
     A a = new A(new B()); // A.<init>(B), B.<init>()
     a.f(); // A.f
     new C().g(); // C.<init>, C.g
     g(new C()); // C.<init>, A.g(IB)
  }
}
    
class Base {
  public void g() { }
}
class B extends Base {
  @Override
  public void g() { }
  
  public void f() {
    g();
  }
}
interface IB {
  void g();
}
interface IB2 extends IB {
  void g2();
}
class C extends B implements IB {
  @Override
  public void g() {
     super.g(); // B.g
  }
  @Override
  public String toString() { return ""; }
}
class D implements IB2 {
  @Override
  public void g() {
  }
  @Override
  public void g2() {
  } 
}
      """

  test("call counter") {
    new CallCounter() {
      val factory = SpoonLauncher(src)
      expectResult(1)(ncalls("A.f:()V"))
      expectResult(6)(ncalls("B.g:()V"))
      expectResult(8)(ncalls("C.g:()V"))
      expectResult(1)(ncalls("A.<init>:(LB;)V"))
      expectResult(1)(ncalls("C.toString:()Ljava/lang/String"))
    }
  }
}