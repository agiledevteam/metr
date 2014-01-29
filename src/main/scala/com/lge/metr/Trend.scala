package com.lge.metr

import java.io.File
import java.io.InputStream
import java.util.Calendar
import scala.language.implicitConversions
import scala.util.Try
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import scala.slick.lifted.TableQuery
import scala.slick.driver.H2Driver.simple._

case class StatEntry(cc: Int, sloc: Int, dloc: Double) extends Values {
  lazy val cn = 100 * (1 - dloc / sloc)
  def values: Seq[Any] = Seq(cn, cc, sloc, dloc)
  def +(b: StatEntry) =
    StatEntry(this.cc + b.cc - 1, this.sloc + b.sloc, this.dloc + b.dloc)
}

object StatEntry {
  val zero = StatEntry(0, 0, 0)
}

class Trend(config: Config) extends MetricCounter {
  val src = config.src
  val out = config.out
  val debug = config.debug

  require(out.exists || out.mkdirs)

  val git = Git(src)
  val relPath = git.relative(src.getAbsoluteFile.toPath)
  val cache = scala.collection.mutable.Map[ObjectId, Option[StatEntry]]()
  val txtGenerator = new TextGenerator(new File(out, "trend.txt"))
  val htmlGenerator = new HtmlGenerator(new File(out, "trend.html"))
  val parser = new ParboiledJavaProcessor

  val projectQuery: TableQuery[Projects] = TableQuery[Projects]
  val commitQuery: TableQuery[Commits] = TableQuery[Commits]
  val objectQuery: TableQuery[Objects] = TableQuery[Objects]

  val db = Database.forURL("jdbc:h2:~/.metr/metr", driver = "org.h2.Driver")
  val projectId = db.withSession { implicit session =>
    scala.util.Try((projectQuery.ddl ++ commitQuery.ddl ++ objectQuery.ddl).create)

    if (!projectQuery.filter(_.gitdir == git.gitDir.toString).exists.run) {
      (projectQuery returning projectQuery.map(_.id)) += (0, "", git.gitDir.toString, "")
    } else {
      projectQuery.filter(_.gitdir == git.gitDir.toString).map(_.id).first
    }
  }

  def metr(exe: JavaModel.Executable): StatEntry =
    StatEntry(cc(exe), sloc(exe).toInt, dloc(exe))

  def metr(input: InputStream): StatEntry =
    parser.process(input).exes.map(metr).foldLeft(StatEntry.zero)(_ + _)

  def metr(obj: Obj): Option[StatEntry] = {
    def calc: Option[StatEntry] = {
      obj match {
        case BlobObj(_, id) =>
          Try(metr(git.repo.open(id).openStream)).toOption
        case TreeObj(_, id) =>
          git.lsTree(id, Suffix.java).foldLeft(Option(StatEntry.zero)) {
            (acc, obj) => for (a <- acc; s <- metr(obj)) yield a + s
          }
      }
    }
    getOrElseUpdate(obj.id, calc)
  }

  def getOrElseUpdate(id: ObjectId, body: => Option[StatEntry]): Option[StatEntry] = {
    db.withSession { implicit session =>
      val strId =id.toString
      val cache = objectQuery.filter(_.sha1 == strId)
      if (cache.exists.run) {
        val p = cache.first
        Some(StatEntry(0, p._2, p._3))
      } else {
        val p = body
        p.foreach(stat => objectQuery += (strId, stat.sloc, stat.dloc))
        p
      }
    }
  }
  //  def gatherNewBlobs(obj: Obj): List[Obj] =
  //    if (cache contains obj.id)
  //      List()
  //    else obj match {
  //      case BlobObj(_, id) => List(obj)
  //      case TreeObj(_, id) => git.lsTree(id, Suffix.java) flatMap gatherNewBlobs
  //    }

  def metr(c: RevCommit): Option[StatEntry] = {
    val obj = git.revParse(c, relPath)
    //gatherNewBlobs(obj) map metr
    metr(obj)
  }

  def commitTime(c: RevCommit): Long = c.getCommitTime.toLong * 1000

  def updateCommit(c: Commit, s: StatEntry) {
    db.withSession { implicit session =>
      commitQuery += (c.id, projectId, c.author, c.timestamp)
    }
  }

  def run(start: String) {
    def toCommit(c: RevCommit): Commit = Commit(c.getId().toString(), c.getAuthorIdent().getEmailAddress(), commitTime(c))

    print("retriving rev-list...(max one year) ")
    val commits = {
      val oneYearBefore = { val c = Calendar.getInstance; c.add(Calendar.YEAR, -1); c.getTimeInMillis }
      val orig = git.revList(start, relPath).
        filter(c => commitTime(c) > oneYearBefore)
      if (debug)
        orig.reverse.take(5)
      else orig
    }
    println(commits.size)

    val trend = commits map { c =>
      val commit = toCommit(c)
      println("processing... "+commit)
      metr(c).map { stat =>
        updateCommit(commit, stat)
        commit -> stat
      }
    } collect {
      case Some(p) => p
    }

    println("generating...")
    txtGenerator.generate(trend.map { p => p._1 ++ p._2 })
    htmlGenerator.generate(trend)
    println("done.")
  }

}
