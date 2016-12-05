package info.kwarc.teaching.AI.Kalah

import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global


/**
  * Abstract class representing a game board. [[Game]] uses its own private extension of this class, as to prevent
  * cheating. Accessible values are:
  *
  * @param houses    : the number of houses per player and
  * @param initSeeds : the initial number of seeds per house
  */
abstract class Board(val houses : Int, val initSeeds : Int) {
  /**
    * Gives the state of the whole gameboard
    * @return a quadruple consisting of
    *         1) The list of numbers of seeds in the houses of Player 1
    *         2) The list of numbers of seeds in the houses of Player 2
    *         3) The number of seeds in Player 1's store
    *         4) The number of seeds in Player 2's store
    */
  def getState : (List[Int],List[Int],Int,Int)

  /**
    * The seeds in each house of some Player
    * @param ag - the agent whose houses to return. Implicit, so that a agent can query
    *           its own houses with no parameters
    * @return the list of seeds for each house.
    */
  def getHouses(implicit ag : Agent) : List[Int]

  /**
    * Gives the number of seeds in a specific house
    * @param player which player's side of the gameboard (has to be either 1 or 2)
    * @param house which house of the specified player (has to be between 1 and houses)
    * @return the number of seeds in the specified house
    */
  def getSeed(player : Int,house : Int) : Int

  /**
    * Gives the current number of seeds in the store of player i
    * @param player has to be either 1 or 2
    * @return number of seeds
    */
  def getScore(player : Int) : Int

  def asString(pl : Agent) : String
}

/**
  * Allows to play one game between two [[Agent]]s. Initializes a game board according to the parameters and
  * calls init on the provided [[Agent]]s. The method "play" starts the game.
  * @param p1         : Agent1
  * @param p2         : Agent2
  * @param houses     : The number of houses per player (see [[Board]])
  * @param initSeeds  : The number of initial seeds per house (see [[Board]])
  */
class Game(p1 : Agent, p2 : Agent)(houses : Int = 6, initSeeds : Int = 6) {

  private object GameBoard extends Board(houses,initSeeds) {
    val p1Houses = mutable.HashMap.empty[Int,Int]
    val p2Houses = mutable.HashMap.empty[Int,Int]
    (1 to houses) foreach (i => {
      p1Houses(i) = initSeeds
      p2Houses(i) = initSeeds
    })
    var p1Store = 0
    var p2Store = 0


    def getHouses(implicit ag : Agent) : List[Int] = ag match {
      case Player1.pl => p1Houses.values.toList
      case Player2.pl => p2Houses.values.toList
    }

    def getState = (p1Houses.values.toList,p2Houses.values.toList,p1Store,p2Store)
    def getSeed(player : Int, house : Int) : Int = {
      require(player ==1 || player == 2)
      require(house >= 1 && house <= houses)
      if (player == 1) p1Houses(house-1)
      else if (player == 2) p2Houses(house-1)
      else throw new Error("Player number not in [1,2]")
    }
    def getScore(player : Int): Int = {
      require(player ==1 || player == 2)
      if (player == 1) p1Store
      else if (player == 2) p2Store
      else throw new Error("Player number not in [1,2]")
    }
    private def doIntStr(i : Int) : String = {
      require(i >= 0 && i <= 1000)
      if (i < 10) "  " + i.toString + "  "
      else if (i < 100) " " + i.toString + "  "
      else " " + i.toString + " "
    }
    def asStringPl(player : Player) : String = {
        "      |" + (1 to houses).map(_ => "-----").mkString("|") + "|\n" +
        "|-----|" + {player match {
          case Player1 => (1 to houses).reverse.map(i => doIntStr(p2Houses(i)) ).mkString("|")
          case Player2 => (1 to houses).reverse.map(i => doIntStr(p1Houses(i)) ).mkString("|")
        }} + "|-----|\n" +
        "|" + { player match {
          case Player1 => doIntStr(p2Store)
          case Player2 => doIntStr(p1Store)
        } } + "|" + (1 to houses).map(_ => "-----").mkString("|") + "|" + { player match {
          case Player1 => doIntStr(p1Store)
          case Player2 => doIntStr(p2Store)
        } } + "|\n" +
          "|-----|" + {player match {
          case Player1 => (1 to houses).map(i => doIntStr(p1Houses(i)) ).mkString("|")
          case Player2 => (1 to houses).map(i => doIntStr(p2Houses(i)) ).mkString("|")
        }} + "|-----|\n" +
          "      |" + (1 to houses).map(_ => "-----").mkString("|") + "|\n" +
        "       " + (1 to houses).map(i => doIntStr(i)).mkString(" ")
    }
    def asString(pl : Agent) : String = pl match {
      case Player1.pl => asStringPl(Player1)
      case Player2.pl => asStringPl(Player2)
      case _ => throw new Error("Unknown Agent: " + pl.getClass)
    }

    override def toString: String = asStringPl(Player1)
  }

  sealed abstract class Player {
    val pl : Agent
    def move : Int = pl.move
    def init : Unit
    def other : Player
  }
  private case object Player1 extends Player {
    val pl = p1
    def init = pl.init(GameBoard,true)
    def other = Player2
  }
  private case object Player2 extends Player {
    val pl = p2
    def init = pl.init(GameBoard,false)
    def other = Player1
  }

  private case class HouseIndex(pl : Player, hn : Int) {
    require(hn >= 0 && hn <= houses)
    def get : Int = (pl,hn) match {
      case (Player1,0) => GameBoard.p1Store
      case (Player2,0) => GameBoard.p2Store
      case (Player1,_) => GameBoard.p1Houses(hn)
      case (Player2,_) => GameBoard.p2Houses(hn)
    }
    def pull : Int = (pl,hn) match {
      case (_,0) => throw new Error("Can not pull store!")
      case (Player1,_) =>
        val ret = GameBoard.p1Houses(hn)
        GameBoard.p1Houses(hn) = 0
        ret
      case (Player2,_) =>
        val ret = GameBoard.p2Houses(hn)
        GameBoard.p2Houses(hn) = 0
        ret
    }
    def ++ : Unit = (pl,hn) match {
      case (Player1,0) => GameBoard.p1Store += 1
      case (Player2,0) => GameBoard.p2Store += 1
      case (Player1,_) => GameBoard.p1Houses(hn) = GameBoard.p1Houses(hn) + 1
      case (Player2,_) => GameBoard.p2Houses(hn) = GameBoard.p2Houses(hn) + 1
    }
    def next(actor : Player) : HouseIndex =
      if (hn == 0) HouseIndex(pl.other,1)
      else if (hn < houses) HouseIndex(pl,hn+1)
      else if (hn == houses && actor == pl) HouseIndex(pl,0)
      else HouseIndex(actor,1)
  }
  private val Store1 = new HouseIndex(Player1,0) {
    def sum = GameBoard.p1Store + (1 to houses).map(i => HouseIndex(Player1,i).get).sum
    def add(i : Int) = GameBoard.p1Store = GameBoard.p1Store + i
  }
  private val Store2 = new HouseIndex(Player2,0) {
    def sum = GameBoard.p2Store + (1 to houses).map(i => HouseIndex(Player2,i).get).sum
    def add(i : Int) = GameBoard.p2Store = GameBoard.p2Store + i
  }
  private def store(pl : Player) = pl match {
    case Player1 => Store1
    case Player2 => Store2
  }

  Player1.init
  Player2.init

  private def playerMove(pl : Player,showboard : Boolean) : String = {
    var ret = ""
    if (showboard) {
      println(pl + ": ")
      println(GameBoard.toString)
    }
    val move = try {
      Await.result(Future {
        pl.move
      }, 5 seconds)
    } catch {
      case e : java.util.concurrent.TimeoutException =>
        ret = pl.pl.name + " timed out!"
        (1 to houses).find(i => HouseIndex(pl,i).get > 0).get
    }
      /*
      val move = future { pl.move }
      Await.result(move, 5 seconds)
      */
    if (!(move >= 1 && move <= houses)) throw Illegal(pl)
    var index = HouseIndex(pl,move)
    val counter = index.pull
    (1 to counter) foreach (_ => {
      index = index.next(pl)
      index.++
    })
    if (index.pl == pl && index.get == 1 && index.hn != 0) {
      store(pl).add(index.pull + HouseIndex(pl.other,houses + 1 - index.hn).pull)
    }
    if (index.pl == pl && index.hn == 0 && (1 to houses).exists(i => HouseIndex(pl,i).get > 0))
      ret + playerMove(pl,showboard) else {
      ret
    }
  }

  case class Illegal(pl : Player) extends Throwable

  private def finished : Option[Player] = if ((1 to houses).forall(i => HouseIndex(Player1,i).get == 0))
    Some(Player2) else if ((1 to houses).forall(i => HouseIndex(Player2,i).get == 0)) Some(Player1)
  else None
  /**
    * Starts one game between (new instances of) Pl1 and Pl2.
    * @return A pair of integers representing the scores of players 1 and 2
    */
  def play(showboard : Boolean = false) : (Int,Int) = {
    try {
      while (finished.isEmpty) {
        val ret = playerMove(Player1, showboard) + {if (finished.isEmpty) playerMove(Player2, showboard) else ""}
        if (!showboard) {
          print("\rScore: " + GameBoard.p1Store + " : " + GameBoard.p2Store + " " + ret)
        }
      }
    } catch {
      case Illegal(Player1) =>
        println("Player1 made illegal move")
        (0,Store2.get + 1)
      case Illegal(Player2) =>
        println("Player2 made illegal move")
        (Store1.get + 1, 0)
    }
    print("\n")
    if (showboard) println(GameBoard.toString)
    if (finished contains Player1) (Store1.sum, Store2.get) else (Store1.get, Store2.sum)
  }
}