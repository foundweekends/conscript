package conscript

object Config extends Launch {
  import java.io.{ FileInputStream, FileOutputStream }
  import java.util.Properties
  
  val file = configdir("config")

  def get(name: String) =
    Option(properties {
      _.getProperty(name)
    })

  def properties[A](f: Properties => A): A = {
    if (!file.exists()) {
      file.getParentFile().mkdirs()
      file.createNewFile()
    }
    val p = new Properties()
    use(new FileInputStream(file)) { in =>
      p.load(in)
    }
    val result = f(p)
    use(new FileOutputStream(file)) { out =>
      p.store(out, null)
    }
    result
  }
  def use[C<:{ def close(): Unit}, T](c: C)(f: C => T) = try {
    f(c)
  } finally {
    c.close
  }
}
