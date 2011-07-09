package conscript

import scala.swing._
import javax.imageio.ImageIO
import java.awt.{Graphics,Color,Font,GraphicsEnvironment,RenderingHints}

trait WritableDisplay {
  def info(msg: String)
  def error(msg: String)
}

trait Display {
  val W = 710
  val H = 210
  lazy val display = new WritableDisplay {
    val img = ImageIO.read(getClass.getResource("/conscript.png"))
    val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAllFonts
    val myfont = new Font("Monospaced", Font.BOLD, 14)

    @volatile var message: Either[String,String] = Right("Starting...")

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
    def info(msg: String) {
      message = Right(msg)
      frame.repaint()
    }
    def error(msg: String) {
      message = Left(msg)
      frame.repaint()
    }
  }
}
