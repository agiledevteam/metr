package com.lge.metr

import java.nio.file.Paths
import java.io.File
import scala.sys.process._

object parser {
  val variablePattern = """DATA((_SLOC|_TIME|_CN)*)""".r
                                                  //> variablePattern  : scala.util.matching.Regex = DATA((_SLOC|_TIME|_CN)*)
  variablePattern.replaceAllIn("DATA_SLOC_CN ab) dkfj", m => (m group 1).split('_').mkString("-"))
                                                  //> res0: String = -SLOC-CN ab) dkfj
  variablePattern.replaceAllIn("data_SLOC_CN ab) dkfj", m => (m group 1).split('_').mkString("-"))
                                                  //> res1: String = data_SLOC_CN ab) dkfj
}