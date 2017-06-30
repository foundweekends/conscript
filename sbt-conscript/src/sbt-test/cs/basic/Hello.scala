package example

import java.io.File

class Hello extends xsbti.AppMain {
  override def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    val a1 :: a2 :: Nil = configuration.arguments.toList
    sbt.io.IO.write(new File(a1), a2)
    new Exit(0)
  }
  class Exit(val code: Int) extends xsbti.Exit
}
