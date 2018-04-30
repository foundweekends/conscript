package conscript

import java.io.{PrintWriter, File}
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.control.Exception._
import scala.util.Random
import java.util.regex.Pattern
import scala.util.matching.Regex

object Clean extends OsDetect {

  /**
   * Windows java runtime can't success with File.delete() while cleaning SBT boot directory.
   * This helper object adds workaround of the issue by the using cmd scripts and windows task scheduler.
   */
  private object Windows {
    lazy val timeFormat = new SimpleDateFormat("HH:mm:ss")

    def exec(args: String*) = allCatch.either { sys.runtime.exec(args.toArray) }

    def writeScript(f:File)(text:String) = {
      var printer = Option.empty[PrintWriter]
      allCatch.andFinally(printer.map(_.close())).either {
        printer = Some(new PrintWriter(f, "UTF-8"))
        text.split("\\n").map(printer.get.println)
      }
    }

    def scheduleClean(file:File) = {
      def time = timeFormat.format(new Date)
      val taskName = "ConscriptClean" + Random.nextInt()
      val cleanScript = File.createTempFile("conscript-clean-boot", ".bat")
      val scheduleScript = File.createTempFile("conscript-schedule-clean", ".bat")
      val createFlags = if (isXP) "/ru SYSTEM " else "/f" // win XP uses different version of schtasks

      for {
        _ <- writeScript(scheduleScript) {
            s"""
              |@echo off
              |schtasks /Create ${createFlags} /tn ${taskName} /sc ONCE /tr "${cleanScript}" /st ${time}
              |schtasks /Run /tn ${taskName}
            """.stripMargin
          } .right
        _ <- writeScript(cleanScript) {
            s"""
              |@echo off
              |schtasks /Delete /f /tn ${taskName}
              |rmdir /q /s "${file}"
              |dir "${file}" && (
              |  "${scheduleScript}"
              |) || (
              |  del /q "${cleanScript}"
              |  del /q "${scheduleScript}"
              |)
            """.stripMargin
          } .right
      } yield {
        sys.addShutdownHook {
          exec(scheduleScript.getAbsolutePath)
        }
        "Clean script scheduled successfully"
      }
    }
  }

  /**
   * Doing recursive removing of the file (or dir).
   *
   * On windows SBT launcher boot directory cannot be cleaned while launcher is running so we do schedule
   * this procedure with the system task manager to run from parallel service process
   *
   * @param file a file to cleanRec
   * @return some error message or none if success
   */
  def clean(file:File):Option[String] =
    cleanRec(file).flatMap { error =>
      windows.map(_ => Windows.scheduleClean(file).left.toOption.map(_.getMessage)) getOrElse(Some(error))
    }

  private def cleanRec(file:File):Option[String] =
    if (file.isDirectory)
      file.listFiles.foldLeft(Option.empty[String]) { (a, f) =>
        a orElse cleanRec(f)
      } .orElse(delete(file))
    else delete(file)

  private def delete(file: File) =
    if (file.delete()) None
    else Some("Unable to delete %s".format(file))

}

