package conscript

object Clean {
  def clean(files: Array[java.io.File]): Option[String] =
    (Option.empty[String] /: files) { (a, e) =>
      a orElse {
        if (e.isDirectory)
          clean(e.listFiles).orElse(delete(e))
        else delete(e)
      }
    }

  private def delete(file: java.io.File) =
    if (file.delete()) None
    else Some("Unable to delete %s".format(file))
}
