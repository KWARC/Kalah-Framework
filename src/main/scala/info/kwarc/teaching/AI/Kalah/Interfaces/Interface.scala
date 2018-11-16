package info.kwarc.teaching.AI.Kalah.Interfaces

import java.awt.event.{ActionEvent, ActionListener, WindowAdapter, WindowEvent}
import java.awt.geom.Rectangle2D
import java.awt.{BasicStroke, BorderLayout, Color, Dimension, Font, Graphics, Graphics2D}
import javax.swing.{JFrame, JPanel, SwingUtilities, Timer}

import info.kwarc.teaching.AI.Kalah.Board
import info.kwarc.teaching.AI.Kalah.util.File

import scala.collection.JavaConverters._
import scala.math._

/**
  * An [[Interface]] is just a trait/class that visualizes a running game in some way, e.g. [[Terminal]] simply
  * prints it in the terminal, [[Fancy]] opens a graphic window and... well, looks nice, I guess.
  */
trait Interface {
  protected var (pl1,pl2) : (String,String) = ("","")
  protected var GameBoard : Board = null
  protected var round = 0

  def write(s : String) : Unit

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
  def illegal(playerOne : Boolean = true,move : Int, board : Board)
  def scoreboard(ls : List[(String,Int)])

  private val THIS_TOP = this
  def + (that : Interface) = CombineInterfaces(THIS_TOP,that)
}

/**
  * If you want to use more than one interface at the same time, this lets you do it.
  * @param a : The first interface
  * @param b : The second interface
  */
case class CombineInterfaces(a : Interface,b : Interface) extends Interface {
  protected def startgame: Unit = ???

  override def write(s: String): Unit = {
    a.write(s)
    b.write(s)
  }
  override def newGame(p1 : String, p2 : String, board : Board) = {
    pl1 = p1
    pl2 = p2
    GameBoard = board
    round = 0
    a.newGame(p1,p2,board)
    b.newGame(p1,p2,board)
  }
  override def endOfRound : Unit = {
    round += 1
    a.endOfRound
    b.endOfRound
  }

  def gameResult(sc1: Int, sc2: Int) = {
    a.gameResult(sc1, sc2)
    b.gameResult(sc1, sc2)
  }

  def timeout(playerOne: Boolean = true): Unit = {
    a.timeout(playerOne)
    b.timeout(playerOne)
  }

  def playerMove(playerOne: Boolean = true) = {
    a.playerMove(playerOne)
    b.playerMove(playerOne)
  }

  def chosenMove(house: Int, playerOne: Boolean = true) = {
    a.chosenMove(house, playerOne)
    b.chosenMove(house, playerOne)
  }

  protected def endRound = ???

  def illegal(playerOne: Boolean = true, move: Int, board: Board): Unit = {
    a.illegal(playerOne, move, board)
    b.illegal(playerOne, move, board)
  }

  def scoreboard(ls: List[(String, Int)]): Unit = {
    a.scoreboard(ls)
    b.scoreboard(ls)
  }
}

/**
  * Prints out everything on the terminal
  */
object Terminal extends Interface {
  def startgame = println(pl1 + " vs. " + pl2)
  def write(s : String) = println(s)
  def gameResult(sc1 : Int, sc2 : Int) = {
    print("\rFinished in round " + round + ". Final score: " + sc1 + " : " + sc2 + "\n")
    if (sc1 > sc2)
      println(pl1 + " wins!")
    else if (sc2 > sc1) println(pl2 + " wins!")
    else println("It's a draw!")
  }
  def timeout(playerOne : Boolean = true) =
    println(pl1 + " timed out during initialization!")

  def playerMove(playerOne : Boolean = true) = {}
  def chosenMove(house : Int, playerOne : Boolean = true) =
    {}//print({if (playerOne) pl1 else pl2} + ": " + house)

  def endRound: Unit = print("\rRound " + round + " Score: " + GameBoard.getScore(1) + " : " + GameBoard.getScore(2) + "  ")

  def illegal(playerOne : Boolean = true, move : Int, board : Board) = {
    println({
      if (playerOne) pl1 else pl2
    } + " made illegal move: " + move)
    println({
      if (playerOne) pl2 else pl1
    } + " wins!")
    println(board.toString)
  }

  override def scoreboard(scs: List[(String, Int)]): Unit =
    println(scs.indices.map(i => i+1 + ": " + scs(i)._1 + "(" + scs(i)._2 + ")").mkString("\n"))
}

/**
  * An interface that logs the progress of a game to a file
  * @param f The file to use.
  */
case class Logger(f : File) extends Interface {
  protected def startgame: Unit = write(pl1 + " vs. " + pl2)
  override def endOfRound : Unit = {
  }

  if(!f.exists()) {
    f.createNewFile()
  }
  def write(s : String) = {
    val bef = File.read(f)
    File.write(f,bef + s + "\n")
  }

  def gameResult(sc1: Int, sc2: Int) = {
    write ("Final score: " + sc1 + ":" + sc2 + " - " +
    {
      if(sc1 > sc2) pl1 + " wins!"
      else if (sc2 > sc1) pl2 + " wins!"
      else "it's a draw!"
    })
  }

  def timeout(playerOne: Boolean = true): Unit = {
    write({if (playerOne) pl1 else pl2} + " timed out!")
  }

  def playerMove(playerOne: Boolean = true) = {

  }

  def chosenMove(house: Int, playerOne: Boolean = true) = {
  }

  protected def endRound = {}

  def illegal(playerOne: Boolean = true, move: Int, board: Board): Unit = {
    write({if (playerOne) pl1 else pl2} + " made illegal move: " + move)
  }

  def scoreboard(ls: List[(String, Int)]): Unit = {

  }
}


object Fancy {

  /**
    * A graphical interface that visualizes the board and the player's moves
    * @param size The font size to use.
    */
  class FancyInterface(var size : Int = 36) extends Interface {
    var slow = true
    private val frame = new FancyFrame(i => (i * size) / 36)
    frame.getContentPane.setLayout(new BorderLayout)
    frame.setVisible(true)

    def th(run : => Unit) = run // SwingUtilities.invokeLater(() => run)

    private def waitforEnd = {
      Thread.sleep(100)
      while(!frame.panel.timer.isDone) {
        Thread.sleep(100)
      }
      if (!slow) Thread.sleep(5000)
    }

    def write(s : String) = {}

    def startgame = {
      frame.init(GameBoard,pl1,pl2)
      Thread.sleep(100)
      println(pl1 + " vs. " + pl2)
    }
    def gameResult(sc1 : Int, sc2 : Int) = {
      val panel = frame.panel
      import panel._
      val st = GameBoard.getState
      th {
        frame.schedule(() => {
          update(st)
          if (sc1 > sc2) status.text = pl1 + " wins! " + sc1 + ":" + sc2
          else if (sc2 > sc1) status.text = pl2 + " wins! " + sc1 + ":" + sc2
          else status.text = "It's a draw!"
          println(status.text)
        })
      }
      waitforEnd
    }
    def timeout(playerOne : Boolean = true) = {
      println("Timeout Player " + {if (playerOne) pl1 else pl2})
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
      val state = GameBoard.getState
      import panel._
      frame.schedule(() => {
        update(state)
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
          timer.sleep(50)

          i += 1
          counter -= 1
          if (counter == 0) {
            val other = {
              var i = pl1houses.indexOf(house)
              val inpl1 = i != -1
              if (!inpl1) i = pl2houses.indexOf(house)
              if (i == -1) None
              else if (playerOne && inpl1) Some(pl2houses(GameBoard.houses - i - 1))
              else if (!playerOne && !inpl1) Some(pl1houses(GameBoard.houses - i - 1))
              else None
            }
            if (other.isDefined) {
              frame.schedule(() => {
                if (other.get.value > 0 && house.value == 1) {
                  house.color = Color.RED
                  other.get.color = Color.RED
                  status.text += "; captures " + {
                    house.value + other.get.value
                  }
                }
              })
            }
            timer.sleep(1000)
            frame.schedule(() => {
              house.color = Color.BLACK
              other.foreach(c => c.color = Color.BLACK)
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
    def endRound = {
      val panel = frame.panel
      val sc = GameBoard.getState
      val r = round + 1
      import panel._
      th {
        frame.schedule(() => {
          top.text = pl1 + " vs. " + pl2 + " Round " + r
          update(sc)
        })
      }
      while (timer.isOverloaded) {
        Thread.sleep(100)
      }
    }
    def illegal(playerOne : Boolean = true, m : Int, board : Board) = {
      println("Illegal move by " + {if (playerOne) pl1 else pl2} + ": " + m)
      val panel = frame.panel
      import panel._
      th {
        frame.schedule(() => {
          if (playerOne) status.text = pl1 + " made illegal move: " + m + " - " + pl2 + " wins!"
          else status.text = pl2 + " made illegal move: " + m + " - " + pl1 + " wins!"
        })
      }
      waitforEnd
    }
    def scoreboard(ls: List[(String, Int)]): Unit =
      println(ls.indices.map(i => i+1 + ": " + ls(i)._1 + "(" + ls(i)._2 + ")").mkString("\n"))

    private def ?? = { frame.repaint() }
  }

  private trait myComponent {
    var color : Color = Color.BLACK
    def draw(g : Graphics2D)
  }

  private class Text(pos_x : Int,pos_y : Int, init : String,scale : Int => Int) extends myComponent {
    var text = init
    def draw(g : Graphics2D): Unit = {
      g.setColor(color)
      g.drawString(text,scale(pos_x),scale(pos_y))
    }

  }

  private class Field(pos_x : Int,pos_y : Int, scale : Int => Int) extends myComponent {
    var value = 100

    def draw(g : Graphics2D): Unit = {
      g.setColor(color)
      g.drawString(value.toString,scale(pos_x+20),scale(pos_y+55))
      g.draw(new Rectangle2D.Double(scale(pos_x),scale(pos_y),scale(100),scale(100)))
    }
  }

  private case class Scoreboard(ls: List[(String, Int)]) extends JPanel {

    val total = ls.length

    lazy val WIDTH = 1024
    lazy val HEIGHT = 768

    val center = (WIDTH/2,HEIGHT/2)
    val length = (WIDTH * 19) / 20

    def getPos(i : Int) = {
      val angle = (3.0 * Pi / 2.0) - (i.toDouble * 2.0 * Pi / total)
      ((length.toDouble * cos(angle)).toInt,(length.toDouble * sin(angle)).toInt)
    }

    case class Node(x : Int, y : Int, pos : Int, plstr : String) {
      def draw(g : Graphics2D) = ???
    }

    val components : List[Node] = ???

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      g.asInstanceOf[Graphics2D].setStroke(new BasicStroke(3))
      g.setFont(new Font("Serif",Font.PLAIN,12))
      components foreach (_.draw(g.asInstanceOf[Graphics2D]))
    }

    setPreferredSize(new Dimension(1024,768))
  }

  private class MyPanel(board :Board,pl1 : String, pl2 : String,scale : Int => Int = i => i) extends JPanel {
    object timer extends ActionListener {
      private val DELAY = 10
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
      def isOverloaded = actions.length > 200
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
    lazy val minwidth = (board.houses + 2) * 100 + 40
    lazy val WIDTH = List(minwidth,(pl1 + " vs. " + pl2 + " Round 99").length * 25 + 40).max
    lazy val HEIGHT = 420

    //override def getPreferredSize: Dimension = new Dimension(scale(WIDTH),scale(HEIGHT))
    setPreferredSize(new Dimension(scale(WIDTH),scale(HEIGHT)))

    val pl2houses = (1 to board.houses).map(i => {
      val box = new Field(20 + (100 * i),120,scale)
      box.value = board.initSeeds
      box
    }).reverse.toList
    val pl2score = new Field(20,170,scale)
    pl2score.value = 0
    val pl1score = new Field(120 + (100 * board.houses),170,scale)
    pl1score.value = 0
    val pl1houses = (1 to board.houses).map(i => {
      val box = new Field(20 + (100 * i),220,scale)
      box.value = board.initSeeds
      box
    }).toList

    val status = new Text(20,392,"Initializing...",scale)
    status.color = Color.RED
    val top = new Text(20,36,pl1 + " vs. " + pl2 + " Round 1",scale)

    val components : List[myComponent] =
      top ::
      new Text((minwidth / 2) - (pl2.length * 10) ,/*76*/95,pl2,scale) ::
      pl1score :: pl2score ::
      new Text((minwidth / 2) - (pl1.length * 10),356,pl1,scale) ::
      status :: (pl1houses ::: pl2houses)

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      g.asInstanceOf[Graphics2D].setStroke(new BasicStroke(scale(3)))
      g.setFont(new Font("Serif",Font.PLAIN,scale(36)))
      components foreach (_.draw(g.asInstanceOf[Graphics2D]))
    }
  }

  private class FancyFrame(scale : Int => Int = i => i) extends JFrame {

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

    def init(board : Board,pl1 : String,pl2 : String) = SwingUtilities.invokeLater(() => {
      if (panel != null) panel.timer.stop
      getContentPane.removeAll()
      panel = new MyPanel(board,pl1,pl2,scale)
      getContentPane.add(panel,BorderLayout.CENTER)
      panel.setVisible(true)
      pack()
    })

    def schedule(f : () => Unit) = panel.timer.schedule(f)

  }
}
