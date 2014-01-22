import scala.sys.process._
import java.io._

case class Config(name: String, repo: String)

val samples = List(
  Config("name", "repo.git"))

s"mkdir /home/chisun/clone".!

samples foreach { c =>
  println("git clone ... " + c.name)
  s"git clone ssh://chisun.joung@lgapps.lge.com:29427/${c.repo} -b LG_apps_master /home/chisun/clone/${c.name}".!
}

println("ok")
