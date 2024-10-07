package sbtconscript

import java.io.File
import java.net.URL

private[sbtconscript] object ConscriptPluginCompat {
  def toURL(u: URL): URL = u
  def toClasspath(f: File): File = f
}
