package conscript

import scala.swing._
import javax.imageio.ImageIO
import java.awt.{Graphics,Color}

trait Display {
  val W = 710
  val H = 210
  def display = {
    val img = ImageIO.read(getClass.getResource("/conscript.png"))
    val frame = new MainFrame {
      title = "Conscript Setup"
      resizable = false
      contents = new Component {
        override def paint(g: Graphics2D) {
          g.clearRect(0, 0, W, H)
          g.drawImage(img, 0, 0, Color.WHITE, null)
        }
        preferredSize = new Dimension(W, H)
      }
      centerOnScreen()
      visible = true
    }
  }
}
