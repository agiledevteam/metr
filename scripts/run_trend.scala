import scala.sys.process._
import java.io._

case class Config(name: String, repo: String) 


val metr = "../target/scala-2.10/metr-assembly-1.0.jar"
val java_opts = "-Xms512m -Xmx512m"

val samples = List(
  Config("Alarm", "LG_apps/android/vendor/lge/apps/AlarmClock4.git"),
  Config("TDMB", "LG_apps/android/vendor/lge/apps/TDMB4.git"),
  Config("FMRadio", "LG_apps/android/vendor/lge/apps/FmRadio4.git"),
  Config("Lifegram", "LG_apps/android/vendor/lge/apps/Lifetracker.git"),
  Config("MeChat", "LG_apps/android/vendor/lge/apps/IntelligentAgent.git"),
  Config("Media_in_One", "LG_apps/android/vendor/lge/appwidget/MediaInOne4.git"),
  Config("TextTranslator", "LG_apps/android/vendor/lge/apps/TextTranslator.git"),
  Config("RCSChat", "LG_apps/android/vendor/lge/apps/RcsChat4.git"),
  Config("RCSStarter", "LG_apps/android/vendor/lge/apps/RcsStarter4.git"),
  Config("TaskManager", "LG_apps/android/vendor/lge/apps/TaskManager4.git"),
  Config("VoLTE", "LG_apps/android/vendor/lge/apps/LTECall4.git"),
  Config("Contacts", "LG_apps/android/vendor/lge/apps/Contacts3_JB.git"),
  Config("File_Networking", "LG_apps/android/vendor/lge/apps/FileNetworking4.git"),
  Config("Launcher", "LG_apps/android/vendor/lge/apps/LGHome4.git"),
  Config("IME", "LG_apps/android/vendor/lge/apps/LGEIME4_0.git"),
  Config("NoteBook", "LG_apps/android/vendor/lge/apps/Notebook4.git"),
  Config("PhotoAlbum", "LG_apps/android/vendor/lge/appwidget/PhotoAlbum3"),
  Config("SafetyCare", "LG_apps/android/vendor/lge/apps/SafetyCare4.git"),
  Config("VidClip", "LG_apps/android/vendor/lge/apps/VidClip4.git"),
  Config("WorldClock", "LG_apps/android/vendor/lge/appwidget/LGWorldClock4.git"),
  Config("Calendar", "LG_apps/android/vendor/lge/apps/Calendar4.git"),
  Config("Camera", "LG_apps/android/vendor/lge/apps/CameraApp3b.git"),
  Config("Easy_UI", "LG_apps/android/vendor/lge/apps/EasyUI_MD.git"),
  Config("Gallery", "LG_apps/android/vendor/lge/apps/GalleryLG.git"),
  Config("LGDSLibrary", "LG_apps/android/vendor/lge/apps/LGDSLibrary.git"),
  Config("LGEmail", "LG_apps/android/vendor/lge/apps/LGEmail4.git"),
  Config("LGMessage", "LG_apps/android/vendor/lge/apps/LGMessage4.git"),
  Config("TagPlus", "LG_apps/android/vendor/lge/apps/LGMessage4.git"),
  Config("VideoTelephony", "LG_apps/android/vendor/lge/apps/VideoTelephony3_JB.git"),
  Config("AAB", "LG_apps/android/vendor/lge/apps/AAB.git"),
  Config("AuHome", "LG_apps/android/vendor/lge/appwidget/AuHome.git"),
  Config("AuLockmedia", "LG_apps/android/vendor/lge/appwidget/AuLockmedia.git"),
  Config("SettingsAccessibility", "LG_apps/android/vendor/lge/apps/SettingsAccessibility2.git"),
  Config("LifeSquare", "LG_apps/android/vendor/lge/apps/LifeStream4.git"),
  Config("LGSetupWizard", "LG_apps/android/vendor/lge/apps/LGSetupWizard_JB2.git"),
  Config("AppCleanup", "LG_apps/android/vendor/lge/apps/AppCleanup.git"),
  Config("ClockWidget", "LG_apps/android/vendor/lge/appwidget/ClockWidget4.git"),
  Config("UnifiedEULA", "LG_apps/android/vendor/lge/apps/UnifiedEula.git"),
  Config("VuTalk", "LG_apps/android/vendor/lge/apps/Vutalk4.git"),
// not include MD work.  
  Config("LGWorld", "LG_apps/android/vendor/lge/apps/LGWorld.git"),
  Config("AppRecovery", "LG_apps/android/vendor/lge/apps/AppRecovery.git"),
  Config("FMRadio", "LG_apps/android/vendor/lge/apps/FmRadio4.git"),
  Config("Help", "LG_apps/android/vendor/lge/apps/LGVZWHelp.git"),
  Config("Call", "LG_apps/android/vendor/lge/apps/Phone4.git"),
  Config("Roaming", "LG_apps/android/vendor/lge/apps/RoamingSettingsKr4_KLP.git"),
  Config("Streaming", "LG_apps/android/vendor/lge/apps/StreamingPlayer4.git"),
  Config("Videos", "LG_apps/android/vendor/lge/apps/VideoPlayer4.git"),
  Config("SmartShare", "LG_apps/android/vendor/lge/apps/ConnectionWizard.git"),
  Config("Camera4", "LG_apps/android/vendor/lge/apps/CameraApp4.git"),
  Config("Qremote", "LG_apps/android/vendor/lge/apps/QRemote4.git"),
  Config("JDMB", "LG_apps/android/vendor/lge/apps/JDMB4.git"),
  Config("LatinDMB4", "LG_apps/android/vendor/lge/apps/LatinDMB4.git"),
  Config("LGUSMms4", "LG_apps/android/vendor/lge/apps/LGUSMms4.git"),
  Config("EAS", "LG_apps/android/vendor/lge/apps/EAS.git"),
  Config("LockScreen4", "LG_apps/android/vendor/lge/apps/LockScreen4.git"),
  Config("EasyHome", "LG_apps/android/vendor/lge/apps/EasyHome"),
  Config("Qmemoplus", "LG_apps/android/vendor/lge/apps/Qmemoplus.git"),
  Config("Richnote3_2", "LG_apps/android/vendor/lge/apps/Richnote3_2.git"),
  Config("Memo3", "LG_apps/android/vendor/lge/appwidget/Memo3.git"),
  Config("QuickMemo4", "LG_apps/android/vendor/lge/apps/QuickMemo4.git"),
  Config("LGTaskApp4", "LG_apps/android/vendor/lge/apps/LGTaskApp4.git"),
  Config("IntelligentAgent", "LG_apps/android/vendor/lge/apps/IntelligentAgent.git"),
  Config("Lifetracker", "LG_apps/android/vendor/lge/apps/Lifetracker.git"),
  Config("QPair", "LG_apps/android/vendor/lge/apps/p2p.git"),
  Config("Weather4", "LG_apps/android/vendor/lge/appwidget/Weather4.git"),
  Config("PersonalAssistant_JB", "LG_apps/android/vendor/lge/apps/PersonalAssistant_JB.git"),
  Config("Settings4_0", "LG_apps/android/vendor/lge/apps/Settings4_0.git"),
  Config("LGVZWSetupWizard_KLP", "LG_apps/android/vendor/lge/apps/LGVZWSetupWizard_KLP.git"),
  Config("BackupRestore4", "LG_apps/android/vendor/lge/apps/BackupRestore4.git"),
  Config("AppBox4", "LG_apps/android/vendor/lge/appwidget/AppBox4.git"),
  Config("ConciergeBoard", "LG_apps/android/vendor/lge/appwidget/ConciergeBoard.git")
 
  
  
  
  
  
) 


samples foreach { c =>
  println("Calculating Code Fat Trend ... "+ c.name)
  s"java $java_opts -jar $metr -t -s /home/chisun/clone/${c.name}/src -d /home/chisun/clone/trend_output/${c.name}".!
}

println("ok")
