package com.lge.metr

import java.io.File
import java.nio.file.Path

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.ListBuffer
import scala.sys.process.fileToProcess

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter

case class Suffix(buf: Array[Byte], len: Int)

object Suffix {
  val java = convert(".java")

  def convert(suffix: String): Suffix = {
    val bytes = suffix.getBytes
    Suffix(bytes, bytes.length)
  }
}

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

  val repo: Repository = new FileRepositoryBuilder().setGitDir(gitDir.toFile).build
  val walk: RevWalk = new RevWalk(repo)

  def relative(path: Path): Option[String] = {
    val relPath = gitWorkTree.relativize(path).toString
    if (relPath.isEmpty) None
    else Some(relPath.toString.replaceAllLiterally(File.separator, "/")) // unixify
  }

  def revList(start: String, path: Option[String]): List[RevCommit] = {
    //println("git rev-list --reverse --date-order " + start+ " -- "+path)
    val walk: RevWalk = new RevWalk(repo)
    val head = walk.parseCommit(repo.resolve(start))
    walk.markStart(head)
    walk.sort(RevSort.COMMIT_TIME_DESC, true)
    walk.sort(RevSort.REVERSE, true)
    path.map(s => walk setTreeFilter (PathFilter create s)) // TODO this doesn't work well
    walk.toList
  }

  def revParse(c: RevCommit, path: Option[String]): (String, ObjectId) = {
    val name = path.getOrElse("")
    name -> repo.resolve(ObjectId.toString(c.getId)+":"+name)
  }

  def lsTree(id: AnyObjectId, suffix: Suffix): List[(String, ObjectId)] = {
    val result = ListBuffer[(String, ObjectId)]()
    val tree = new TreeWalk(repo)
    tree.addTree(id)
    tree.setRecursive(false)
    while (tree.next()) {
      if (tree.isSubtree || tree.isPathSuffix(suffix.buf, suffix.len))
        result += Tuple2(tree.getNameString, tree.getObjectId(0))
    }
    result.toList
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
