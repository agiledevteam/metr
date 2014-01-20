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
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Constants
import scala.util._
import java.util.Date
import java.util.Calendar

case class StatEntry(cn: Double, cc: Int, sloc: Int, dloc: Double) extends Values {
  def values: Seq[Any] = Seq(cn, cc, sloc, dloc)
}
object StatEntry {
  val zero = StatEntry(0, 0, 0, 0)
  def plus(a: StatEntry, b: StatEntry) = {
    val dloc = a.dloc + b.dloc
    val sloc = a.sloc + b.sloc
    val cc = a.cc + b.cc - 1
    val cn = 100 * (1 - dloc / sloc)
    StatEntry(cn, cc, sloc, dloc)
  }
}

class Trend(src: File, out: File, debug: Boolean) {
  require(out.exists || out.mkdirs)
  val git = Git(src)
  val relPath = git.relative(src.getAbsoluteFile.toPath)
  val txtGenerator = new TextGenerator(new File(out, "trend.txt"))
  val htmlGenerator = new HtmlGenerator(new File(out, "trend.html"))
  val cache = scala.collection.mutable.Map[String, StatEntry]()

  var counter = 0

  def metr(entity: (String, ObjectId)): StatEntry = {
    val (name, id) = entity
    def metr_(): StatEntry = {
      val loader = git.repo.open(id)
      loader.getType match {
        case Constants.OBJ_BLOB =>
          counter += 1
          Metric(loader.openStream).stat
        case Constants.OBJ_TREE =>
          git.lsTree(id, Suffix.java).map(metr(_)).foldLeft(StatEntry.zero)(StatEntry.plus)
      }
    }
    cache getOrElseUpdate (ObjectId.toString(id), metr_)
  }

  def metr(c: RevCommit): Try[StatEntry] = {
    counter = 0
    val t = Try(metr(git.revParse(c, relPath)))
    println(".. " + counter)
    t
  }

  def commitTime(c: RevCommit): Long = c.getCommitTime.toLong * 1000

  def run(start: String) {
    def toCommit(c: RevCommit): Commit = Commit(commitTime(c), c.getId.abbreviate(6).name)

    print("retriving rev-list...(max one year) ")
    val commits = {
      val oneYearBefore = { val c = Calendar.getInstance; c.add(Calendar.YEAR, -1); c.getTimeInMillis }
      val orig = git.revList(start, relPath).
        filter(c => commitTime(c) > oneYearBefore)
      if (debug) orig.take(5) else orig
    }
    println(commits.size)

    val trend = commits map { c =>
      val commit = toCommit(c)
      println("processing... "+commit)
      metr(c).map(toCommit(c) -> _)
    }

    println("generating...")
    txtGenerator.generate(trend.collect { case Success(p) => p._1 ++ p._2 })
    htmlGenerator.generate(trend.collect { case Success(p) => p })
    println("done.")
  }

}
