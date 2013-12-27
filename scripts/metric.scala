import scala.io.Source
import scala.math.Numeric
import java.io._

  case class Rec(sloc: Double, dloc: Double, ncalls: Int, cc: Int, method: String)

  type Db = List[Rec]
  def load(path: String): Db = {
    Source.fromFile(path).getLines.map { line =>
      val v = line.split("\t")
      Rec(v(0).toDouble, v(1).toDouble, 0, 0, v(3))
    }.toList
  }

 val dbs =  new File("output").listFiles.map(f => f.getPath() + "/report.txt")

  def report(name: String)(calc: Db => Double) {
    println(name)
    dbs.foreach { db =>
      println(calc(load(db)) + "\t" + db)
    }
  }

  report("(sloc - dloc)/sloc : gap / as-is") { db =>
    1 - db.map(_.dloc).sum / db.map(_.sloc).sum
  }

