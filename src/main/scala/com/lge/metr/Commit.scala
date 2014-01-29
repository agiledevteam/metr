package com.lge.metr

import java.text.SimpleDateFormat

case class Commit(id: String, author: String, timestamp: Long) extends Values {
  private val df = new SimpleDateFormat()
  override def toString: String = {
    s"Commit(${id.take(7)}, $author, ${df.format(timestamp)})" 
  }
  def values =
    Seq(timestamp, id)
}