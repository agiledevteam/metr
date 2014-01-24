
import scala.io.Source
import scala.sys.process._
import java.io._

case class Config(name: String, repo: String) 


val samples = List(
     Config("name","repo.git")) 


samples foreach { c =>
  print(c.name +  "  ")
  println(Source.fromFile(s"/home/chisun/clone/trend_output/${c.name}/trend.txt").getLines.toList.last)
}

println("ok")
