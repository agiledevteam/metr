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
class CallCountTest extends FunSuite with CallCounter {
  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile("public class Test{}; \n"+src, "Test.java")
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
    val f = Loader.load(src)
    val m = ncalls(f)
    f.all[CtAbstractInvocation[_]].filterNot(_.isImplicit).foreach(in ¢¡ println(in.getExecutable))
    println(m)
    expect(1)(m("A.f:()V"))
    expect(5)(m("B.g:()V"))
    expect(7)(m("C.g:()V"))
    expect(1)(m("A.<init>:(LB;)V"))
    expect(1)(m("C.toString:()Ljava/lang/String"))
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