package com.lge.metr

import java.io.File

object FileUtil {
  def gatherFiles(f: File, ext: String): Seq[File] =
    if (f.isDirectory)
      f.listFiles.flatMap(gatherFiles(_, ext))
    else if (f.getPath.endsWith(ext))
      Seq(f)
    else
      Seq()
  def java_gatherFiles(f: File, ext: String) = gatherFiles(f, ext).toArray[File]
}