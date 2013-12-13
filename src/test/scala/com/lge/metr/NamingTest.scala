package com.lge.metr

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spoon.reflect.Factory
import spoon.support.builder.support.CtVirtualFile
import spoon.support.builder.CtResource
import spoon.reflect.code.CtAbstractInvocation
import spoon.reflect.visitor.Query
import spoon.reflect.visitor.filter.AbstractFilter

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class NamingTest extends FunSuite with Naming {

  class InvokeFilter[T](cname: String, mname: String) extends AbstractFilter[CtAbstractInvocation[T]](classOf[CtAbstractInvocation[T]]) {
    override def matches(t: CtAbstractInvocation[T]): Boolean = {
      t.getExecutable.getDeclaringType.getSimpleName == cname && t.getExecutable.getSimpleName == mname
    }
  }

  def invokeTo(m: Factory, cname: String, mname: String): CtAbstractInvocation[_] = {
    Query.getElements(m, new InvokeFilter(cname, mname)).toList.head
  }

  implicit def sourceToResource(src: String): CtResource = {
    new CtVirtualFile(src, "")
  }

  test("method names") {
    val src = """
package test;
      
class A {
  int a;
  B b;
  A(B b) {
     a = 0;
     this.b = b;
  }
      
  class C {
      B c() {
        return new B();
      }
  }
      
      
  void f() {
     b.g(1);
     b.g(2);
  }
      
  static void d(int[] ia) {
      System.out.println(ia.length);
  }
  public static void main(String args[]) {
     A a = new A(new B());
     a.f();
     a.new C().c();
     A.d(new int[]{1,2,3});
  }
}	
class B {
  void g(int c) {
    ;
  }
}
      """
    val f = Loader.load(src)
    expect("test/B.g:(I;)V")(nameFor(invokeTo(f, "B", "g").getExecutable))
    expect("test/A.f:()V")(nameFor(invokeTo(f, "A", "f").getExecutable))
    expect("test/A$C.c:()Ltest/B")(nameFor(invokeTo(f, "C", "c").getExecutable))
    expect("test/A.d:([I;)V")(nameFor(invokeTo(f, "A", "d").getExecutable))
    expect("test/A.<init>:(Ltest/B;)V")(nameFor(invokeTo(f, "A", "<init>").getExecutable))
  }
}