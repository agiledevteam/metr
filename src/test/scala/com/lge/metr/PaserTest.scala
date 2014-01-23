package com.lge.metr

import scala.language.implicitConversions
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.lge.metr.JavaModel._

@RunWith(classOf[JUnitRunner])
class ParserTest extends FunSuite {
  test("parse input") {
    val p = new ParboiledJavaProcessor
    val cu = p.process("""
interface foo {
        int bar();
  }
class Test {
  abstract int bar();
  void f() {
        ;
    final int a = 3;
    int b = 3;
    int c;
        {
    c = a + b;
  }
    try {
        c = a / 0;
  } catch (IOException e) {
  } catch (Exception e) {
  } finally {
        try {}
        catch (Exception e) {}
  }
   synchronized(this) {  
    if (c > 0) {
        return;
    } else {
        c++;
    }
  }
        while (true);
    do c++; while (c<10);
        la:
    for (int i=0; i<10; i++) {
        continue la;
    }
    new Thread(new Runnable() {
       @Override
       public void run() {
          switch(a) {
        case 0:
        case 1:
           break;
        case 2:
        {
        System.out.println("");
        }
         break;
        default:
           break;
  } //switch
  } //run
  }).start(); //Runnable
        } //f
} //Test
        """)
    expectResult(2)(cu.exes.size)
//    expectResult(CompUnit(List(
//      Method("f", BlockStmt(List(
//        OtherStmt(),
//        OtherStmt(),
//        OtherStmt(),
//        OtherStmt(),
//        IfStmt(BlockStmt(List(OtherStmt())), None),
//        OtherStmt()))),
//      Method("run", BlockStmt(List())))))(cu)
  }
}