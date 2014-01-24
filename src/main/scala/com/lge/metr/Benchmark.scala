package com.lge.metr

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.collection.parallel.ParSeq

object Benchmark extends App {
  val cache = scala.collection.concurrent.TrieMap[File, Unit]()

  def time[T](body: => T): T = {
    val begin = System.nanoTime
    val t = body
    val gap = System.nanoTime - begin
    println(Duration.fromNanos(gap).toMillis)
    t
  }

  def first[T](fs: Future[T]*): Future[T] = {
    val p = Promise[T]()
    for (f <- fs) { f onComplete { t => p.tryComplete(t) } }
    p.future
  }

  val antlr = new AntlrJavaProcessor
  val parboiled = new ParboiledJavaProcessor

  val files = FileUtil.gatherFiles(new File(args(0)), ".java")
  def run(p: JavaProcessor) {
    time {
      Await.ready(Future.sequence(files.map { f =>
        val input = InputUtil.readAllText(f)
        Future {
          p.process(input)
          print(".")
        }
      }), Duration.Inf)
    }
  }
  run(antlr)
  run(parboiled)

}