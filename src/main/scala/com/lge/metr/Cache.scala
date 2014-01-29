package com.lge.metr

trait Cache[Key, Value] {
  def getOrElseUpdate(key: Key, body: =>Value): Value
}