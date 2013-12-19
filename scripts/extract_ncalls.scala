import scala.io._
import java.io._

Source.fromFile(new File(args(0))).getLines.drop(5) foreach { line =>
  val items = line.split("\t")
  println(items.last + "\t" + items.head)
}
