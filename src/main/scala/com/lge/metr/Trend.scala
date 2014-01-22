package com.lge.metr

import java.io.File
import java.util.Calendar

import scala.collection.concurrent.TrieMap
import scala.language.implicitConversions
import scala.util.Success
import scala.util.Try

import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit

case class StatEntry(cc: Int, sloc: Int, dloc: Double) extends Values {
  lazy val cn = 100 * (1 - dloc / sloc)
  def values: Seq[Any] = Seq(cn, cc, sloc, dloc)
  def +(b: StatEntry) =
    StatEntry(this.cc + b.cc - 1, this.sloc + b.sloc, this.dloc + b.dloc)
}

object StatEntry {
  val zero = StatEntry(0, 0, 0)
}

class Trend(src: File, out: File, debug: Boolean) {
  require(out.exists || out.mkdirs)
  val git = Git(src)
  val relPath = git.relative(src.getAbsoluteFile.toPath)
  val txtGenerator = new TextGenerator(new File(out, "trend.txt"))
  val htmlGenerator = new HtmlGenerator(new File(out, "trend.html"))
  val cache = TrieMap[ObjectId, Option[StatEntry]]()

  def metr(obj: Obj): Option[StatEntry] = {
    def calc: Option[StatEntry] = {
      obj match {
        case BlobObj(_, id) =>
          Try(Metric(git.repo.open(id).openStream).stat).toOption
        case TreeObj(_, id) =>
          git.lsTree(id, Suffix.java).foldLeft(Option(StatEntry.zero)) {
            (acc, obj) => for (a <- acc; s <- metr(obj)) yield a + s
          }
      }
    }
    cache getOrElseUpdate (obj.id, calc)
  }

  def gatherNewBlobs(obj: Obj): List[Obj] =
    if (cache contains obj.id)
      List()
    else obj match {
      case BlobObj(_, id) => List(obj)
      case TreeObj(_, id) => git.lsTree(id, Suffix.java) flatMap gatherNewBlobs
    }

  def metr(c: RevCommit): Option[StatEntry] = {
    val obj = git.revParse(c, relPath)
    gatherNewBlobs(obj) foreach metr
    metr(obj)
  }

  def commitTime(c: RevCommit): Long = c.getCommitTime.toLong * 1000

  def run(start: String) {
    def toCommit(c: RevCommit): Commit = Commit(commitTime(c), c.getId.abbreviate(7).name)

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
      println("processing... " + commit)
      metr(c).map(toCommit(c) -> _)
    }

    println("generating...")
    txtGenerator.generate(trend.collect { case Some(p) => p._1 ++ p._2 })
    htmlGenerator.generate(trend.collect { case Some(p) => p })
    println("done.")
  }

}
