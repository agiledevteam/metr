package com.lge.metr

import java.text.SimpleDateFormat

case class Commit(timestamp: Long, commitId: String) extends Values {
  private val df = new SimpleDateFormat()
  override def toString: String = {
    s"Commit(${df.format(timestamp)}, $commitId)" 
  }
  def values: Seq[Any] = Seq(timestamp, commitId)
}