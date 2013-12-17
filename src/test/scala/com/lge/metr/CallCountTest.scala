package com.lge.metr

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spoon.support.builder.CtResource
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.declaration.CtInterface
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.declaration.CtMethod

@RunWith(classOf[JUnitRunner])
class CallCountTest extends FunSuite with CallCounter {
  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile("public class Test{}; \n"+src, "Test.java")
  }

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
  void g() { }
}
class B extends Base {
  @Override
  void g() { }
}
interface IB {
  void g();
}
class C extends B implements IB {
  @Override
  void g() {
     super.g(); // B.g
  }
  @Override
  public String toString() { return ""; }
}
      """
  val factory = Loader.load(src)

  test("implementing interface method") {
    val methods = factory.all[CtMethod[_]]
    val ifmethod = methods.find(nameFor(_) == "IB.g:()V").get
    val implementers = methods.filter(_.isImplementing(ifmethod.getReference))
    expect(1)(implementers.size)
    expect("C.g:()V")(nameFor(implementers.head))
  }

  test("overriding") {
    val methods = factory.all[CtMethod[_]]
    val ifmethod = methods.find(nameFor(_) == "B.g:()V").get
    val overridings = methods.filter(_.isOverriding(ifmethod.getReference))
    expect(2)(overridings.size)
    expect(Set("C.g:()V", "B.g:()V"))(overridings.map(nameFor(_)).toSet)
  }

  test("call counter") {
    val ncalls = ncallsMap
    expect(1)(ncalls("A.f:()V"))
    expect(5)(ncalls("B.g:()V"))
    expect(7)(ncalls("C.g:()V"))
    expect(1)(ncalls("A.<init>:(LB;)V"))
    expect(1)(ncalls("C.toString:()Ljava/lang/String"))
  }
}