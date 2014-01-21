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
  val cache = TrieMap[ObjectId, StatEntry]()

  def metr(obj: Obj): StatEntry = {
    def calc = {
      obj match {
        case BlobObj(_, id) => ??? // some things gone wrong
        case TreeObj(_, id) =>
          val values = git.lsTree(id, Suffix.java) map metr 
          values.foldLeft(StatEntry.zero)(_ + _)
      }
    }
    cache getOrElseUpdate (obj.id, calc)
  }

  def analyzeBlobs(obj: Obj) {
    def gatherBlobs(obj: Obj): List[ObjectId] = {
      if (cache.contains(obj.id))
        List()
      else obj match {
        case BlobObj(_, id) => List(id)
        case TreeObj(_, id) => git.lsTree(id, Suffix.java) flatMap gatherBlobs
      }
    }
    val blobs = gatherBlobs(obj)
    println(s".. found ${blobs.size} blobs")
    blobs.foreach { id => cache update (id, Metric(git.repo.open(id).openStream).stat) }
  }

  /** util for performance measuring */
  def time[A](a: => A) = {
    val now = System.nanoTime
    val result = a
    val micros = (System.nanoTime - now) / 1000
    println("%d microseconds".format(micros))
    result
  }

  def metr(c: RevCommit): Try[StatEntry] = {
    val obj = git.revParse(c, relPath)
    Try(analyzeBlobs(obj)).map(_ => metr(obj))
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
      println("processing... "+commit)
      metr(c).map(toCommit(c) -> _)
    }

    println("generating...")
    txtGenerator.generate(trend.collect { case Success(p) => p._1 ++ p._2 })
    htmlGenerator.generate(trend.collect { case Success(p) => p })
    println("done.")
  }

}
