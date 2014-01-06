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

class Trend(src: File, out: File) {
  val git = Git(src)
  val relPath = git.relative(src.getAbsoluteFile.toPath)
  val txtGenerator = new TextGenerator(new File(out, "trend.txt"))
  val htmlGenerator = new HtmlGenerator(new File(out, "trend.html"))
  val cache = scala.collection.mutable.Map[String, StatEntry]()

  def metr(id: ObjectId): StatEntry = {
    def metr_(): StatEntry = {
      val loader = git.repo.open(id)
      loader.getType match {
        case Constants.OBJ_BLOB =>
          Metric(loader.openStream).stat
        case Constants.OBJ_TREE =>
          git.lsTree(id, Suffix.java).map(metr(_)).foldLeft(StatEntry.zero)(StatEntry.plus)
      }
    }
    cache getOrElseUpdate (ObjectId.toString(id), metr_)
  }

  def metr(c: RevCommit): StatEntry = {
    metr(git.revParse(c, relPath))
  }

  def run(debug: Boolean) {
    def toCommit(c: RevCommit): Commit = Commit(c.getCommitTime.toLong * 1000, c.getId.abbreviate(6).name)

    print("retriving rev-list... ")
    val commits = {
      val orig = git revList relPath
      if (debug) orig.take(5) else orig
    }
    println(commits.size)

    val trend = commits map { c =>
      val commit = toCommit(c)
      println("processing... "+commit)
      commit -> metr(c)
    }

    println("generating...")
    txtGenerator.generate(trend.map(p => p._1 ++ p._2))
    htmlGenerator.generate(trend)
    println("done.")
  }

}