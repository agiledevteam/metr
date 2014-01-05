package com.lge.metr

import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class StatEntry(cn: Double, cc: Int, sloc: Int, dloc: Double) extends Values {
  def values: Seq[Any] = Seq(cn, cc, sloc, dloc)
}

class Trend(src: File, out: File) {
  require(new File(out, "commits").exists() || new File(out, "commits").mkdirs())

  val git = Git(src)
  val relPath = git.relative(src.getAbsoluteFile.toPath)
  val txtGenerator = new TextGenerator(new File(out, "trend.txt"))
  val htmlGenerator = new HtmlGenerator(new File(out, "trend.html"))

  def checkoutSource(c: Commit, tempDir: Path) {
    git.checkoutTo(c, relPath, tempDir)
  }

  def metr(c: Commit, tempDir: Path): StatEntry = {
    val m = Metric(tempDir.toFile)
    println("metr done: "+c.toString)
    tempDir.toFile.delete
    val reportFile = out.toPath.resolve(Paths.get("commits", c.commitId+".txt"))
    m.generate(reportFile.toFile)
    m.stat
  }

  def run() {
    val commits = git.revList(relPath)
    val trend = for (c <- commits) yield {
      val tempDir = Files.createTempDirectory(null)
      checkoutSource(c, tempDir)
      Future { c -> metr(c, tempDir) }
    }
    val t = Await.result(Future.sequence(trend), Duration.Inf)
    println("generating...")
    txtGenerator.generate(t.map(p => p._1 ++ p._2))
    htmlGenerator.generate(t)
  }

}