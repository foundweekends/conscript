package sbtconscript

import java.io.File
import java.net.URL
import java.net.URI
import java.nio.file.Path

private[sbtconscript] object ConscriptPluginCompat {
  def toURL(u: URI): URL = u.toURL
  def toClasspath(f: File): Path = f.toPath
}
