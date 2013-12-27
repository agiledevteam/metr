import scala.sys.process._
import java.io._

val metr = "../target/scala-2.10/metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"
val reports = Seq("sloc", "dloc", "cc")

case class Config(src: File) {
  def cmd: String =
    s"-s ${src.getPath}"
  def name: String = src.getPath.split(File.separator)(1).toLowerCase
}

val targets = new File(".").listFiles.filter(f => f.isDirectory).
	map(f => new File(f, "src")).filter(_.exists).map(Config(_))

println(targets)

targets foreach { t =>
  println("processing... "+t.name)
  s"java $java_opts -jar $metr ${t.cmd}".!

  val dest = s"output/${t.name}"
  println(s"moving results to $dest")
  s"mkdir -p $dest".!
  Seq("bash", "-c", s"mv *.txt $dest").!
}

println("ok")
