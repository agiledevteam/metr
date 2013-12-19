package com.lge.metr

import scala.collection.mutable.ListBuffer
import java.io.File

abstract class Task(name: String) {
  val dependencies = ListBuffer[Task]()

  def generate() {
    dependencies foreach (_.generate)
    if (!getFile.exists)
      doGenerate
  }

  def doGenerate

  def dependOn(others: Task*): Task = {
    dependencies ++= others
    this
  }

  def clean() {
    getFile.delete()
  }

  def getFile: File = new File(name + ".txt")
}

object Task {
  def apply(name: String)(body: Task => Unit): Task = new Task(name) {
    def doGenerate() {
      body(this)
    }
  }
}
