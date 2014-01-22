package com.lge.metr

import java.io.InputStream

import com.lge.metr.JavaModel.CompUnit

trait JavaProcessor {
  def process(input: InputStream): CompUnit
}