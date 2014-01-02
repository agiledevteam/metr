import scala.io.Source
import scala.math.Numeric
import java.io._

  case class Rec(sloc: Double, dloc: Double, ncalls: Int, cc: Int, method: String)

  type Db = List[Rec]
  def load(path: String): Db = {
    Source.fromFile(path).getLines.map { line =>
      val v = line.split("\t")
      Rec(v(0).toDouble, v(1).toDouble, 0, v(2).toInt, v(3))
    }.toList
  }

  val dbs = if (args.size > 0) {
    args
  } else {
    new File("output").listFiles.map(f => f.getPath() + "/report.txt")
  }

  def report(name: String)(calc: Db => List[Double]) {
    println(name)
    dbs.foreach { db =>
      println(calc(load(db)).map("%10.1f".format(_)).mkString("\t") + "\t" + db)
    }
  }

  report("(sloc - dloc)/sloc : gap / as-is") { db =>
    List(
        db.map(_.sloc).sum,
        db.map(_.dloc).sum,
        db.map(_.cc - 1).sum + 1,
        (1 - db.map(_.dloc).sum / db.map(_.sloc).sum) * 100
        )
  }

