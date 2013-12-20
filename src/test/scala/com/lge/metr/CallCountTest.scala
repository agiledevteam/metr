package com.lge.metr

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.language.implicitConversions
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtSimpleType
import spoon.reflect.reference.CtTypeReference
import spoon.support.builder.CtResource
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.declaration.CtType
import org.scalatest.junit.JUnitRunner
import scala.collection.mutable.ListBuffer
import spoon.reflect.declaration.CtMethod
import spoon.reflect.declaration.CtExecutable

@RunWith(classOf[JUnitRunner])
class CallCountTest extends FunSuite with CallCounter with Naming {
  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile("public class Test{}; \n" + src, "Test.java")
  }
  implicit def nameToExecutable(name: String): CtExecutable[_] = {
    factory.all[CtExecutable[_]].find(nameFor(_) == name).get
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

  val factory = Loader.load(src)

  test("call counter") {
    expectResult(1)(ncalls("A.f:()V"))
    expectResult(5)(ncalls("B.g:()V"))
    expectResult(7)(ncalls("C.g:()V"))
    expectResult(1)(ncalls("A.<init>:(LB;)V"))
    expectResult(1)(ncalls("C.toString:()Ljava/lang/String"))
  }
}