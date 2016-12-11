package info.kwarc.teaching.AI.Kalah

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
/*
object Fancy {
  class FancyInterface extends Interface {
    val frame = new FancyFrame
    frame.getContentPane.setLayout(new BorderLayout)
    frame.setVisible(true)

    def th(run : => Unit) = SwingUtilities.invokeLater(() => run)
    def startgame = th {
      frame.init(GameBoard)
    }
    def gameResult(sc1 : Int, sc2 : Int) = ??
    def timeout(playerOne : Boolean = true) = ??
    def playerMove(playerOne : Boolean = true) = ??
    def chosenMove(house : Int, playerOne : Boolean = true) = ??
    def endRound = ??
    def illegal(playerOne : Boolean = true) = ??

    private def ?? = th { frame.repaint() }
  }

  class MyPanel(board :Board) extends JPanel with ActionListener {
    def actionPerformed(e : ActionEvent) = repaint()
    // setLayout(new BorderLayout)
    val DELAY = 150
    val timer = new Timer(DELAY,this)
    timer.start()
    // setOpaque(true)
    override def getPreferredSize: Dimension = new Dimension(800,600)
    setPreferredSize(new Dimension(800,600))

    override def paintComponent(g: Graphics): Unit = {
      // println("in paintComponent")
      super.paintComponent(g)
      //gr.setPaint(Color.BLACK)
      g.drawLine(5,5,795,595)
      g.drawLine(200,30,30,200)
      g.drawLine(200,30,30,200)
    }
  }

  class FancyFrame extends JFrame {
    private val DELAY = 150

    var panel: MyPanel = null

    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        if (panel != null) panel.timer.stop()
      }
    })

    setTitle("Kalah")
    // setSize(new Dimension(1000,700))
    setLocationRelativeTo(null)
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    def init(board : Board) = {
      if (panel != null) panel.timer.stop()
      getContentPane.removeAll()
      panel = new MyPanel(board)
      getContentPane.add(panel,BorderLayout.CENTER)
      panel.setVisible(true)
      pack()
    }
  }
}
*/