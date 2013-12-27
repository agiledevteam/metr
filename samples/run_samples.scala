import scala.sys.process._
import java.io._

case class Config(name: String, src: String) 

val metr = "../target/scala-2.10/metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"
val samples = List(
  Config("github-android", "github-android/app/src/main/java"),
  Config("google-iosched", "google-iosched/android/src/main/java")
) 

samples foreach { c =>
  println("processing... "+ c.name)
  s"java $java_opts -jar $metr -s ${c.src}".!

  val dest = s"output/${c.name}"
  println(s"moving results to $dest")
  s"mkdir -p $dest".!
  Seq("bash", "-c", s"mv *.txt $dest").!
}

println("ok")
