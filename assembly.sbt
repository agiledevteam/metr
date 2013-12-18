import AssemblyKeys._ // put this at the top of the file

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList(ps @ _*) if ps.last == "JDTCompiler$Compiler.class"  => MergeStrategy.first
    case PathList(ps @ _*) if ps.last == "JDTCompiler.class"  => MergeStrategy.first
    case x => old(x)
  }
}
