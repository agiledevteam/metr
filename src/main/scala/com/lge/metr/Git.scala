package com.lge.metr

import java.io.File
import java.nio.file.Paths
import scala.sys.process.fileToProcess
import scala.sys.process.stringSeqToProcess
import rx.lang.scala.Observable
import rx.lang.scala.Subscription
import java.nio.file.Path
import rx.lang.scala.subjects.ReplaySubject

class Git(gitWorkTree: Path) {
  require(gitWorkTree.isAbsolute)

  val gitDir: Path = {
    val dotGit = gitWorkTree.resolve(".git")
    if (dotGit.toFile.isDirectory) dotGit
    else {
      val parent = dotGit.getParent
      val props = Map() ++ dotGit.toFile.cat.lines.map { line =>
        val pair = line.span(_ != ' ')
        pair._1 -> pair._2.trim
      }
      props.get("gitdir:").map(parent resolve _).get.normalize
    }
  }

  val gitDirOption = "--git-dir="+gitDir

  def relative(path: Path): Path = gitWorkTree.relativize(path)

  def revList(path: Path): List[Commit] = {
    val cmd = Seq("git", gitDirOption, "rev-list", "HEAD", "--timestamp", "--reverse", "--date-order", "--", path.toString)
    println(cmd.mkString(" "))
    for (line <- cmd.lines.toList) yield {
      val fields = line.split(" ")
      Commit(fields(0).toLong * 1000, fields(1))
    }
  }

  def checkoutTo(commit: Commit, path: Path, dest: Path): Unit = this.synchronized {
    val workTreeOption = "--work-tree="+dest
    val cmd = Seq("git", gitDirOption, workTreeOption, "checkout", commit.commitId, "--", path.toString)
    println(cmd.mkString(" "))
    cmd.!
  }
}

object Git {
  def apply(src: File): Git = {
    findGitWorkTree(src) match {
      case Some(git) => new Git(git.toPath)
      case None => throw new Error("can't find git from: "+src.getPath)
    }
  }
  def findGitWorkTree(src: File): Option[File] = {
    def recur(dir: File): Option[File] = {
      val dotGit = new File(dir, ".git")
      if (dotGit.exists) {
        Some(dir)
      } else {
        Option(dir.getParentFile).flatMap(recur(_))
      }
    }
    recur(src.getAbsoluteFile)
  }

}
