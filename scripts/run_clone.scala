import scala.sys.process._
import java.io._

case class Config(name: String, repo: String) 

val samples = List(
  Config("EmPubLite", "git@github.com:chisun-joung/EmPubLite.git"),
  Config("Androidgoos", "git@github.com:chisun-joung/androidgoos.git")
) 


s"mkdir /Users/csjoung/clone".!


samples foreach { c =>
  println("git clone ... "+ c.name)
  s"git clone ${c.repo} /Users/csjoung/clone/${c.name}".!
}

println("ok")
