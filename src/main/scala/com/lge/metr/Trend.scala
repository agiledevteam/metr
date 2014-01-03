package com.lge.metr

import java.io.File
import scala.sys.process._

class Trend(src: File, out: File) {
  type CommitId = String
  def run() {
    val commits: Stream[CommitId] = gitlog
    
  }
  
  def gitlog: Stream[CommitId] = {
    Seq("git", "--git-dir=/.git", "rev-list", "master", "--", "src").lines
  }
}