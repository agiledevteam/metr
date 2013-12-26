import scala.io.Source
import scala.math.Numeric

object AppMain extends App {
  case class Rec(sloc: Double, dloc: Double, ncalls: Int, cc: Int, method: String)

  type Db = List[Rec]
  def load(path: String): Db = {
    Source.fromFile(path).getLines.map { line =>
      val v = line.split("\t")
      Rec(v(0).toDouble, v(1).toDouble, v(2).toInt, v(3).toInt, v(4))
    }.toList
  }

  val dbs = List(
    "output/notebook4/report.txt",
    "output/alarmclock4/report.txt",
    "output/deskclock/report.txt",
    "output/google-iosched/report.txt",
    "output/github-android/report.txt")
  def report(name: String)(calc: Db => Double) {
    println(name)
    dbs.foreach { db =>
      println(calc(load(db)) + "\t" + db)
    }
  }

  report("(sloc - dloc)/sloc : gap / as-is") { db =>
    1 - db.map(_.dloc).sum / db.map(_.sloc).sum
  }
  report("(sloc - dloc)/dloc : gap / to-be") { db =>
    db.map(_.sloc).sum / db.map(_.dloc).sum - 1
  }

  //  report("1-sloc/dloc*ncalls") { db =>
  //    1 - db.map(_.sloc).sum / db.map(r => r.dloc * r.ncalls).sum
  //  }
  //  report("sloc") { db =>
  //    db.map(r => r.sloc).sum 
  //  }
  report("max(cc - 10, 0)") { db =>
    db.map(r => (r.cc - 10) max 0).sum
  }
  report("max(cc - 10, 0) / sloc") { db =>
    db.map(r => (r.cc - 10) max 0).sum / db.map(_.sloc).sum
  }
  report("max(cc - 10, 0) / dloc") { db =>
    db.map(r => (r.cc - 10) max 0).sum / db.map(_.dloc).sum
  }
  report("cc / sloc") { db =>
    db.map(r => r.cc).sum / db.map(_.sloc).sum
  }
  report("cc / dloc") { db =>
    db.map(r => r.cc).sum / db.map(_.dloc).sum
  }
  report("(sloc-dloc)*ncalls / sloc") { db =>
    db.map(r => (r.sloc - r.dloc) * r.ncalls).sum / db.map(_.sloc).sum
  }

}