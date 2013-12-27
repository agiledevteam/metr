import scala.sys.process._
import java.io._

val metr = "../target/scala-2.10/metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"

case class Config(src: File) {
  def name: String = src.getPath.split(File.separator)(1).toLowerCase
}

val targets = new File(".").listFiles.filter(f => f.isDirectory).
	map(f => new File(f, "src")).filter(_.exists).map(Config(_))

println(targets)

targets.par foreach { t =>
  println("processing... "+t.name)
  
  val dest = s"output/${t.name}"
  s"mkdir -p $dest".!

  s"java $java_opts -jar $metr -s ${t.src.getPath} -o $dest/report.txt".!
}

println("ok")
