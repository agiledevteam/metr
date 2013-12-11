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
import spoon.support.builder.CtFile
import spoon.support.builder.support.CtFileFile
import java.io.File
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class DlocTest extends FunSuite with LocCounter {

  object launcher extends AbstractLauncher {

    def load(res: CtResource): Factory = {
      val f = createFactory();
      val b = f.getBuilder
      b.addInputSource(res)
      b.build
      f
    }
  }

  def stringResource(src: String): CtFile =
    new CtVirtualFile(src, "Loc.java")

  def fileResource(path: String): CtFile =
    new CtFileFile(new File(path))

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
    val f = launcher.load(stringResource(testSrc(src)))
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
    expect(5)(dloc(body))
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
    expect(4.5)(dloc(body))
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
    expect(11.5)(dloc(body))
  }

  def checkFile(testFile: String, testMethod: String) {
    val res = fileResource(testFile)
    val f = launcher.load(res)
    val weightP = "// ?([.0-9]+)".r
    val expected = Source.fromFile(testFile).getLines
      .map(weightP findFirstIn _)
      .collect {
        case Some(weightP(w)) => w.toDouble
      }.foldLeft(0.0)(_ + _)
    expect(expected)(dloc(methodNamed(f, testMethod).getBody))
  }

  test("sample input ") {
    checkFile("src/test/resources/LittleNested.java", "nestedCases")
    checkFile("src/test/resources/AllGrammar.java", "allStatements")
  }
}