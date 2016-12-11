package info.kwarc.teaching.AI.Kalah

import java.awt.{BasicStroke, BorderLayout, Color, Dimension, Font, Graphics, Graphics2D}
import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import java.awt.geom.Rectangle2D
import javax.swing.{JFrame, JPanel, SwingUtilities, Timer}
import collection.JavaConverters._

/**
  * Created by jazzpirate on 11.12.16.
  */
trait Interface {
  protected var (pl1,pl2) : (String,String) = ("","")
  protected var GameBoard : Board = null
  protected var round = 0

  def newGame(p1 : String, p2 : String, board : Board) = {
    pl1 = p1
    pl2 = p2
    GameBoard = board
    round = 0
    startgame
  }
  def endOfRound : Unit = {
    round += 1
    endRound
  }

  protected def startgame : Unit
  def gameResult(sc1 : Int, sc2 : Int)
  def timeout(playerOne : Boolean = true)
  def playerMove(playerOne : Boolean = true)
  def chosenMove(house : Int, playerOne : Boolean = true)
  protected def endRound
  def illegal(playerOne : Boolean = true)
  def scoreboard(ls : List[(String,Int)])
}

object Terminal extends Interface {
  protected def startgame = println(pl1 + " vs. " + pl2)
  def gameResult(sc1 : Int, sc2 : Int) = {
    print("\rFinished in round " + round + ". Final score: " + sc1 + " : " + sc2 + "\n")
    if (sc1 > sc2)
      println(pl1 + " wins!")
    else println(pl2 + " wins!")
  }
  def timeout(playerOne : Boolean = true) =
    println(pl1 + " timed out during initialization!")

  def playerMove(playerOne : Boolean = true) = {}
  def chosenMove(house : Int, playerOne : Boolean = true) =
    {}//print({if (playerOne) pl1 else pl2} + ": " + house)

  def endRound: Unit = print("\rRound " + round + " Score: " + GameBoard.getScore(1) + " : " + GameBoard.getScore(2))

  def illegal(playerOne : Boolean = true) = {
    println({
      if (playerOne) pl1 else pl2
    } + " made illegal move")
    println({
      if (playerOne) pl2 else pl1
    } + " wins!")
  }

  override def scoreboard(scs: List[(String, Int)]): Unit =
    println(scs.indices.map(i => i+1 + ": " + scs(i)._1 + "(" + scs(i)._2 + ")").mkString("\n"))
}

object Fancy {
  class FancyInterface extends Interface {
    var slow = true
    val frame = new FancyFrame
    frame.getContentPane.setLayout(new BorderLayout)
    frame.setVisible(true)

    def th(run : => Unit) = run //SwingUtilities.invokeLater(() => run)

    private def waitforEnd = {
      Thread.sleep(100)
      while(!frame.panel.timer.isDone) {
        Thread.sleep(100)
      }
      Thread.sleep(5000)
    }

    def startgame = {
      frame.init(GameBoard,pl1,pl2)
    }
    def gameResult(sc1 : Int, sc2 : Int) = {
      val panel = frame.panel
      import panel._
      th {
        frame.schedule(() => {
          update(GameBoard.getState)
          if (sc1 > sc2) status.text = pl1 + " wins! " + sc1 + ":" + sc2
          else status.text = pl2 + " wins! " + sc1 + ":" + sc2
        })
      }
      waitforEnd
    }
    def timeout(playerOne : Boolean = true) = {
      val panel = frame.panel
      import panel._
      th {
        frame.schedule(() => {
          update(GameBoard.getState)
          if (playerOne) status.text = pl1 + " timed out! " + pl2 + " wins!"
          else status.text = pl2 + " timed out! " + pl1 + " wins!"
        })
      }
      waitforEnd
    }
    def playerMove(playerOne : Boolean = true) = th {
      val panel = frame.panel
      import panel._
      frame.schedule(() => {
        if (playerOne) status.text = pl1 + "..."
        else status.text = pl2   + "..."
      })
    }
    def chosenMove(housei : Int, playerOne : Boolean = true) = th {
      val panel = frame.panel
      import panel._
      val start = if (playerOne) pl1houses(housei-1) else pl2houses(housei-1)
      frame.schedule(() => {
        if (playerOne) status.text = pl1 + "... picks " + housei
        else status.text = pl2   + "... picks " + housei
        start.color = Color.green
      })
      var (p1,i,counter) = (playerOne,housei,if (playerOne) GameBoard.getSeed(1,housei) else GameBoard.getSeed(2,housei))
      timer.sleep(if (slow) 950 else 200)
      frame.schedule(() => {
        start.value = 0
      })
      if (slow) {
        while (counter > 0) {
          val (house, pred) = if (i == 0 && p1 && playerOne) (pl1houses.head, pl2houses.last)
          else if (i == 0 && p1) (pl1houses.head, pl2score)
          else if (i == 0 && playerOne) (pl2houses.head, pl1score)
          else if (i == 0) (pl2houses.head, pl1houses.last)
          else if (i == GameBoard.houses && p1 && playerOne) {
            i = -1
            p1 = false
            (pl1score, pl1houses.last)
          } else if (i == GameBoard.houses && p1) {
            i = 0
            p1 = false
            (pl2houses.head, pl1houses.last)
          } else if (i == GameBoard.houses && playerOne) {
            i = 0
            p1 = true
            (pl1houses.head, pl2houses.last)
          } else if (i == GameBoard.houses) {
            i = -1
            p1 = true
            (pl2score, pl2houses.last)
          } else if (p1) (pl1houses(i), pl1houses(i - 1))
          else (pl2houses(i), pl2houses(i - 1))

          frame.schedule(() => {
            house.value += 1
            house.color = Color.ORANGE
            pred.color = if (pred == start) Color.green else Color.BLACK
          })
          timer.sleep(200)

          i += 1
          counter -= 1
          if (counter == 0) {
            timer.sleep(200)
            frame.schedule(() => {
              house.color = Color.BLACK
              start.color = Color.BLACK
            })
          }
        }
      } else {
        val ret = GameBoard.getState
        frame.schedule(() => {
          start.color = Color.BLACK
          update(ret)
        })
      }
    }
    def endRound = th {
      val panel = frame.panel
      val sc = GameBoard.getState
      val r = round + 1
      import panel._
      frame.schedule(() => {
        top.text = pl1 + " vs. " + pl2 + " Round " + r
        update(sc)
      })
    }
    def illegal(playerOne : Boolean = true) = {
      val panel = frame.panel
      import panel._
      th {
        frame.schedule(() => {
          if (playerOne) status.text = pl1 + " made illegal move! " + pl2 + " wins!"
          if (playerOne) status.text = pl2 + " made illegal move! " + pl1 + " wins!"
        })
      }
      waitforEnd
    }
    def scoreboard(ls: List[(String, Int)]): Unit = ??

    private def ?? = { frame.repaint() }
  }

  trait myComponent {
    var color : Color = Color.BLACK
    def draw(g : Graphics2D)
  }

  class Text(pos_x : Int,pos_y : Int, init : String) extends myComponent {
    var text = init
    def draw(g : Graphics2D): Unit = {
      g.setColor(color)
      g.drawString(text,pos_x,pos_y)
    }

  }

  class Field(pos_x : Int,pos_y : Int) extends myComponent {
    var value = 100

    def draw(g : Graphics2D): Unit = {
      g.setColor(color)
      g.drawString(value.toString,pos_x+20,pos_y+55)
      g.draw(new Rectangle2D.Double(pos_x,pos_y,100,100))
    }
  }

  class MyPanel(board :Board,pl1 : String, pl2 : String) extends JPanel {
    object timer extends ActionListener {
      private val DELAY = 20
      private val t = new Timer(DELAY,this)
      t.start()
      private var actions : List[() => Unit] = Nil

      def sleep(s : Int) = actions = actions ::: (1 to (s/DELAY)).toList.map(_ => () => {})

      def schedule(f : () => Unit) = actions = actions ::: List(f)
      def actionPerformed(e : ActionEvent) = if(actions.isEmpty) repaint() else {
        actions.head.apply()
        actions = actions.tail
        repaint()
      }
      def stop = t.stop()
      def isDone = actions.isEmpty
    }

    def update(res : (java.lang.Iterable[Int],java.lang.Iterable[Int],Int,Int)) = {
      val (p1h,p2h,sc1,sc2) = res
      (0 until board.houses).foreach(i => {
        pl1houses(i).value = p1h.asScala.toList(i)
        pl2houses(i).value = p2h.asScala.toList(i)
      })
      pl1score.value = sc1
      pl2score.value = sc2
      repaint()
    }

    val WIDTH = (board.houses + 2) * 100 + 40
    val HEIGHT = 420

    override def getPreferredSize: Dimension = new Dimension(WIDTH,HEIGHT)
    setPreferredSize(new Dimension(WIDTH,HEIGHT))

    val pl2houses = (1 to board.houses).map(i => {
      val box = new Field(20 + (100 * i),120)
      box.value = board.initSeeds
      box
    }).reverse.toList
    val pl2score = new Field(20,170)
    pl2score.value = 0
    val pl1score = new Field(120 + (100 * board.houses),170)
    pl1score.value = 0
    val pl1houses = (1 to board.houses).map(i => {
      val box = new Field(20 + (100 * i),220)
      box.value = board.initSeeds
      box
    }).toList

    val status = new Text(20,392,"Initializing...")
    status.color = Color.RED
    val top = new Text(20,36,pl1 + " vs. " + pl2 + " Round 1")

    val components : List[myComponent] =
      top ::
      new Text((WIDTH / 2) - (pl2.length * 10) ,/*76*/95,pl2) ::
      pl1score :: pl2score ::
      new Text((WIDTH / 2) - (pl1.length * 10),356,pl1) ::
      status :: (pl1houses ::: pl2houses)

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      g.asInstanceOf[Graphics2D].setStroke(new BasicStroke(3))
      g.setFont(new Font("Serif",Font.PLAIN,36))
      components foreach (_.draw(g.asInstanceOf[Graphics2D]))
    }
  }

  class FancyFrame extends JFrame {

    var panel: MyPanel = null

    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        if (panel != null) panel.timer.stop
      }
    })

    setTitle("Kalah")
    // setSize(new Dimension(1000,700))
    setLocationRelativeTo(null)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    def init(board : Board,pl1 : String,pl2 : String) = {
      if (panel != null) panel.timer.stop
      getContentPane.removeAll()
      panel = new MyPanel(board,pl1,pl2)
      getContentPane.add(panel,BorderLayout.CENTER)
      panel.setVisible(true)
      pack()
    }

    def schedule(f : () => Unit) = panel.timer.schedule(f)

  }
}
