import scala.sys.process._
import java.io._

val metr = "metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"
val reports = Seq("sloc", "dloc", "cc")

case class Config(src: String, deps: Seq[String]) {
  def cmd: String =
    s"-s $src -d ${deps.mkString(File.pathSeparator)} -t ${reports.mkString(File.pathSeparator)}"
  def name: String = src.split(File.separator)(0).toLowerCase
}

val targets = scala.collection.mutable.ListBuffer[Config]()

targets += Config(
  src = "Notebook4/src",
  deps =
    List("Notebook4/external-libs/android_kk.jar",
      "Notebook4/bin/Notebook4_master_ReadOnly-dex2jar.jar",
      "Notebook4/external-libs/lgapi_LAPKR131211.jar",
      "Notebook4/external-libs/maps.jar",
      "sdk/add-ons/addon-google_apis-google-18/effects.jar",
      "sdk/add-ons/addon-google_apis-google-18/usb.jar",
      "sdk/android-18/android.jar"))

targets += Config(
  src = "AlarmClock4/src",
  deps =
    List("sdk/android-18/android.jar",
      "AlarmClock4/bin/DefaultAlarmClockActivity-dex2jar.jar"))

targets += Config(
  src = "DeskClock/src",
  deps =
    List("sdk/android-19/android.jar",
      "DeskClock/bin/DeskClock-dex2jar.jar"))

targets += Config(
  src = "github-android/app/src/main/java",
  deps =
    List("sdk/android-16/android.jar",
      "github-android-1.8.0-dex2jar.jar"))

targets += Config(
  src = "google-iosched/android/src/main/java",
  deps =
    List("sdk/android-18/android.jar",
      "google-iosched/android/build/apk/android-debug-unaligned-dex2jar.jar"))

def isSpecified(t: Config): Boolean = {
  if (args.size > 0) {
    args.exists(a => t.name.startsWith(a.toLowerCase))
  } else {
    true
  }
}

targets filter (isSpecified(_)) foreach { t =>
  println("processing... "+t.name)
  s"java $java_opts -jar $metr ${t.cmd}".!

  val dest = s"output/${t.name}"
  println(s"moving results to $dest")
  s"mkdir -p $dest".!
  Seq("bash", "-c", s"mv *.txt $dest").!
}

println("ok")
