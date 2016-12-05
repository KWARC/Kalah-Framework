package info.kwarc.teaching.AI.Kalah

/**
  * The abstract class for agents.
  * @param name     : What you call your agent (just for fun). Please make this one fixed (unlike in the example
  *                   below)
  * @param students : Please put your full names in this list.
  */
abstract class Agent(val name : String, val students : List[String]) {
  /**
    * is called once at the start of a game.
    * @param board The [[Board]] used (states in particular the parameters)
    * @param playerOne is true iff this agent is playerOne in the current game
    */
  def init(board : Board, playerOne : Boolean)

  /**
    * This method is called by [[Game]] to request an action.
    * @return An integer between 1 and n representing the house to play.
    */
  def move : Int
}

/**
  * A human player that reads actions (as field numbers) from stdin. Can be used to test your agents.
  * @param name     : What you call your agent (just for fun).
  */
class HumanPlayer(name : String) extends Agent(name,List("Dennis")) {
  private var currentboard : Board = null
  private var playerone = None.asInstanceOf[Option[Boolean]]

  def init(board : Board, playerOne : Boolean): Unit = {
    currentboard = board
    playerone = Some(playerOne)
    println("Initializing for " + name)
    println("Playing Kalah(" + board.houses + "," + board.initSeeds + ")")
  }

  def move : Int = {
    println("Your move, " + name)
    println(currentboard.asString(this))
    readLine("Enter house: ").toInt
  }
}