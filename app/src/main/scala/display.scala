package conscript

import scala.swing._
import javax.imageio.ImageIO
import java.awt.{Graphics,Color,Font,GraphicsEnvironment,RenderingHints}

trait Display {
  def info(msg: String)
  def error(msg: String)
}
object ConsoleDisplay extends Display {
  def info(msg: String) {
    println(msg)
  }
  def error(msg: String) {
    System.err.println(msg)
  }
}
object SplashDisplay extends Display {
  val W = 710
  val H = 210
  @volatile private var message: Either[String,String] =
    Right("Starting...")

  def info(msg: String) {
    message = Right(msg)
    display.frame.repaint()
  }
  def error(msg: String) {
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
        override def paint(g: Graphics2D) {
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
          })
          g.drawString(msg, 10, H - 10)
        }
        preferredSize = new Dimension(W, H)
      }
      centerOnScreen()
      visible = true
    }
  }
}
