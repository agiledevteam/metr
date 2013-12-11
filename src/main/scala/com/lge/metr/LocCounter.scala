package com.lge.metr

trait LocCounter {

  def blankLine(l: String) = l forall (!_.isLetterOrDigit)

  def plainLoc(s: String) = (s.lines filterNot blankLine).size

}