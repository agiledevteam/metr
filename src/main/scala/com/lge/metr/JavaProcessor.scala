package com.lge.metr

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset

import com.lge.metr.JavaModel.CompUnit

trait JavaProcessor {
  def process(input: File): CompUnit = process(InputUtil.readAllText(input))
  def process(input: InputStream): CompUnit = process(InputUtil.readAllText(input, Charset.forName("UTF8")))
  def process(input: String): CompUnit = process(input.toCharArray)
  def process(input: Array[Char]): CompUnit = process(String.valueOf(input))
}