package com.lge.metr
import scala.collection.JavaConversions._
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spoon.AbstractLauncher
import spoon.reflect.declaration.CtMethod
import spoon.support.builder.CtResource
import spoon.reflect.declaration.CtPackage
import spoon.support.builder.support.CtVirtualFile
import spoon.reflect.visitor.Query
import spoon.reflect.Factory
import spoon.reflect.visitor.Filter
import spoon.reflect.visitor.filter.AbstractFilter

@RunWith(classOf[JUnitRunner])
class DLOCVisitorTest extends FunSuite with LocCounter {

  object launcher extends AbstractLauncher {

    def load(src: String): Factory = {
      val f = createFactory();
      val b = f.getBuilder
      b.addInputSource(asResource(src))
      b.build
      f
    }

    def asResource(src: String): CtResource = new CtVirtualFile(src, "Loc.java")
  }

  class MethodFilter(name: String) extends AbstractFilter[CtMethod[_]](classOf[CtMethod[_]]) {
    override def matches(m: CtMethod[_]): Boolean = {
      m.getSimpleName == name
    }
  }

  def methodNamed(f: Factory, name: String): CtMethod[_] = {
    Query.getElements(f, new MethodFilter(name)).head
  }

  def testSrc(src: String): String = {
    val header =
      """class Loc {
        | public void loc() {
        """.stripMargin
    val footer =
      """
        | }
        |}""".stripMargin
    header + src + footer
  }

  def dloc(src: String): Double = {
    val f = launcher.load(testSrc(src))
    dloc(methodNamed(f, "loc").getBody)
  }

  test("straight forward plain loc") {
    val body = """
      int a;
      return;
      """
    expect(2)(dloc(body))
  }

  test("if-else plain loc") {
    val body = """
      if (true) {
        int a = 0;
        a++;
      }
      return;
      """
    expect(3)(dloc(body))
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
    expect(5)(dloc(body))
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
    expect(6)(dloc(body))
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
    expect(4)(dloc(body))
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
    expect(5)(dloc(body))
  }
}