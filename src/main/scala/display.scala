package conscript

import scala.swing._
import javax.imageio.ImageIO
import java.awt.{Color,Font,GraphicsEnvironment,RenderingHints}

trait Display {
  def info(msg: String): Unit
  def error(msg: String): Unit
}
object ConsoleDisplay extends Display {
  def info(msg: String): Unit = {
    println(msg)
  }
  def error(msg: String): Unit = {
    System.err.println(msg)
  }
}
object SplashDisplay extends Display {
  val W = 710
  val H = 210
  @volatile private var message: Either[String,String] =
    Right("Starting...")

  def info(msg: String): Unit = {
    message = Right(msg)
    display.frame.repaint()
  }
  def error(msg: String): Unit = {
    message = Left(msg)
    display.frame.repaint()
  }

  val display = new {
    val img = ImageIO.read(getClass.getResource("/conscript.png"))
    val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAllFonts
    val myfont = new Font("Monospaced", Font.BOLD, 14)

    val frame = new MainFrame {
      title = "Conscript Setup"
      resizable = false
      contents = new Component {
        override def paint(g: Graphics2D): Unit = {
          g.clearRect(0, 0, W, H)
          g.drawImage(img, 0, 0, Color.WHITE, null)
          g.setFont(myfont)
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
          val msg = message.fold({ err =>
            g.setColor(Color.RED)
            err
          }, { info =>
            g.setColor(Color.BLACK)
            info
          }).split("\n")
          msg.zipWithIndex.foreach { case (line, i) =>
            g.drawString(line, 8, H + 8 - 18*(msg.length - i))
          }
        }
        preferredSize = new Dimension(W, H)
      }
      centerOnScreen()
      visible = true
    }
  }
}
