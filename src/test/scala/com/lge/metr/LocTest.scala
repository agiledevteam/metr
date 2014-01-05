package com.lge.metr

import java.io.File

import scala.collection.JavaConversions._
import scala.io.Source

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.language.implicitConversions

@RunWith(classOf[JUnitRunner])
class LocTest extends FunSuite with MetricCounter with MetricTest {

  test("straight forward plain loc") {
    val body = """
      Runnable a = new Runnable() {
        @Override
        public void run() {
          ;
        }
      };
      return;
      """
    expectResult(2)(dloc(body))
    expectResult(2)(sloc(body))
  }

  test("if-else plain loc") {
    val body = """
      if (true) {
        int a = 0;
        a++;
      }
      return;
      """
    expectResult(3)(dloc(body))
  }

  test("if-else-if plain loc") {
    val body = """
      if (true) {
        int a = 0;
        a++;
      } else if (false) {
        int a = 0;
        a++;
      }
      return;
      """
    expectResult(5)(dloc(body))
  }
  test("if-else-if-else(empty) plain loc") {
    val body = """
        if (true) {
          int a = 0;
          a++;
        } else if (false) {
          int a = 0;
          a++;
        } else {
        }
        return;
        """
    expectResult(6)(dloc(body))
  }
  test("nested-if") {
    val body = """
        if (true) {
          int a = 0;
          a++;
          if (false) {
            int b = 0;
            b++;
          }
        }
        return;
        """
    expectResult(4)(dloc(body))
  }

  test("nested block") {
    val body = """
        if (true) { //1
          int a = 0;
          a++;       //2
          {
            int b = 0;
            b++;  // 3
            b++; 
            b++;  // 4
          }
        }
        return; // 5
        """
    expectResult(5)(dloc(body))
  }

  test("loop") {
    val body = """
        int c = 3;
        while (c < 3) { //1
          int a = 0;
          a++;       //2
          do {
            int b = 0;
            b++;  // 3
            b++; 
            b++;  // 4
          } while (a < 0);
        }
        """
    expectResult(5)(dloc(body))
  }

  test("synchronized") {
    val body = """
        if (true) { //  1
          int a = 0; // 0.5
          a++;       // 0.5
          synchronized(this) {  // 0.5
            int b = 0;  // 0.5
            b++;        // 0.5
          }
        }
        return; // 1
        """
    expectResult(4.5)(dloc(body))
  }

  test("switch-case") {
    val body = """
          int a = 3;
          switch(a) {
      case 0: 
      case 2:
        a++;
        a++;
      case 3:
        if (a < 3) {
          a++;
          a++;
        }
      case 5:
        a++;
        a++;
        a++;
        break;
      default:
        break;
       }
          """
    expectResult(11.5)(dloc(body))
  }

  test("do-while-one-line") {
    val body = """
      int a = 0;
      do a--; while(a>0);
      """
    expectResult(3.5)(dloc(body))
    expectResult(4)(sloc(body))
  }

  def checkFile(testFile: String, testMethod: String) {
    val weightP = "// ?([.0-9]+)".r
    val weights = Source.fromFile(testFile).getLines
      .map(weightP findFirstIn _)
      .collect {
        case Some(weightP(w)) => w.toDouble
      }.toList
    val m = new Metric
    val cu = m.parse(new FileResource(new File(testFile)).inputStream)
    val e = m.findExecutableIn(cu).find(_.name.contains(testMethod)).get
    expectResult(weights.sum)(dloc(e))
    expectResult(weights.filter(_ != 0).size)(sloc(e))
  }

  test("sample input ") {
    checkFile("src/test/resources/LittleNested.java", "nestedCases")
    checkFile("src/test/resources/AllGrammar.java", "allStatements")
  }
}