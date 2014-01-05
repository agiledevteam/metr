package com.lge.metr

trait Values {
  self =>
  def values: Seq[Any]
  def ++[B <: Values](that: B): Values = new Values {
    def values: Seq[Any] =
      self.values ++ that.values
  }
}