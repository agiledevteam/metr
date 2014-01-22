package com.lge.metr

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset

import com.lge.metr.JavaModel.CompUnit

trait JavaProcessor {
  def process(input: InputStream): CompUnit = process(InputUtil.readAllText(input, Charset.forName("UTF8")))
  def process(input: String): CompUnit = process(new ByteArrayInputStream(input.getBytes))
}