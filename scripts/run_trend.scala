import scala.sys.process._
import java.io._

case class Config(name: String, repo: String) 


val metr = "../target/scala-2.10/metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"

val samples = List(
  Config("name", "repo.git")) 


samples foreach { c =>
  println("Calculating Code Fat Trend ... "+ c.name)
  s"java $java_opts -jar $metr -t -s /home/chisun/clone/${c.name}/src -d /home/chisun/clone/trend_output/${c.name}".!
}

println("ok")
