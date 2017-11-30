package info.kwarc.teaching.AI.Kalah

import java.io._

import info.kwarc.teaching.AI.Kalah.Agents.{Agent, HumanPlayer}
import info.kwarc.teaching.AI.Kalah.Interfaces.Interface
import info.kwarc.teaching.AI.Kalah.util._

import scala.collection.mutable
import collection.JavaConverters._


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
  def getState : (java.lang.Iterable[Int],java.lang.Iterable[Int],Int,Int)

  /**
    * The seeds in each house of some Player
    * @param ag - the agent whose houses to return. Implicit, so that a agent can query
    *           its own houses with no parameters
    * @return the list of seeds for each house.
    */
  def getHouses(implicit ag : Agent) : java.lang.Iterable[Int]

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

  def getMoves : java.lang.Iterable[(Boolean,Int)]
}

/**
  * Allows to play one game between two [[Agent]]s. Initializes a game board according to the parameters and
  * calls init on the provided [[Agent]]s. The method "play" starts the game.
  * @param p1         : Agent1
  * @param p2         : Agent2
  * @param houses     : The number of houses per player (see [[Board]])
  * @param initSeeds  : The number of initial seeds per house (see [[Board]])
  */
class Game(p1 : Agent, p2 : Agent,interface : Interface)(houses : Int = 6, initSeeds : Int = 6) {

  private object GameBoard extends Board(houses,initSeeds) {
    var moves : List[(Player,Int)] = Nil
    val p1Houses = mutable.HashMap.empty[Int,Int]
    val p2Houses = mutable.HashMap.empty[Int,Int]
    (1 to houses) foreach (i => {
      p1Houses(i) = initSeeds
      p2Houses(i) = initSeeds
    })
    var p1Store = 0
    var p2Store = 0

    def getMoves = moves.map({
      case (Player1,i) => (true,i)
      case (Player2,i) => (false,i)
    }).asJava

    /*
    private def getlastmovesrecurse(list: List[(Player,Int)], pl : Player) : List[Int] =
      if (list.isEmpty) Nil
      else if (list.head._1 == pl) list.head._2 :: getlastmovesrecurse(list.tail,pl)
      else List(list.head._2)
    override def getLastMoves(player: Int): List[Int] = player match {
      case 1 => getlastmovesrecurse(moves,Player1).reverse
      case 2 => getlastmovesrecurse(moves,Player2).reverse
      case _ => throw new Exception("Not a player: " + player)
    }
    */

    private def valuelist(pl : Player) = pl match {
      case Player1 => (1 to houses).map(p1Houses.apply).toList
      case Player2 => (1 to houses).map(p2Houses.apply).toList
    }

    def getHouses(implicit ag : Agent) : java.lang.Iterable[Int] = ag match {
      case Player1.pl => valuelist(Player1).asJava
      case Player2.pl => valuelist(Player2).asJava
    }

    def getState = (valuelist(Player1).asJava,valuelist(Player2).asJava,p1Store,p2Store)
    def getSeed(player : Int, house : Int) : Int = {
      require(player ==1 || player == 2)
      require(house >= 1 && house <= houses)
      if (player == 1) p1Houses(house)
      else if (player == 2) p2Houses(house)
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
    def other = Player2
    def init = AgentAction.init(pl,GameBoard,true,10000)//pl.init(GameBoard,true)
  }
  private case object Player2 extends Player {
    val pl = p2
    def other = Player1
    def init = AgentAction.init(pl,GameBoard,false,10000)//pl.init(GameBoard,true)
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
    def sum = {
      GameBoard.p1Store = GameBoard.p1Store + (1 to houses).map(i => HouseIndex(Player1,i).pull).sum
      GameBoard.p1Store
    }
    def add(i : Int) = GameBoard.p1Store = GameBoard.p1Store + i
  }
  private val Store2 = new HouseIndex(Player2,0) {
    def sum = {
      GameBoard.p2Store = GameBoard.p2Store + (1 to houses).map(i => HouseIndex(Player2,i).pull).sum
      GameBoard.p2Store
    }
    def add(i : Int) = GameBoard.p2Store = GameBoard.p2Store + i
  }
  private def store(pl : Player) = pl match {
    case Player1 => Store1
    case Player2 => Store2
  }

  private def playerMove(pl : Player) : Unit = {
    interface.playerMove(pl == Player1)
    val move = try {
        pl.pl match {
          case hp : HumanPlayer =>
            AgentAction.move(pl.pl,1000000)
          case _ =>
            AgentAction.move(pl.pl,5000)
        }
      } catch {
      case _: Throwable =>
        Thread.sleep(50)
        try {
          pl.pl.timeoutMove
        } catch {
          case _: Throwable => throw Illegal(pl, -20)
        }
    }
    if (!(move >= 1 && move <= houses && HouseIndex(pl,move).get > 0)) throw Illegal(pl,move)
    GameBoard.moves ::= (pl,move)
    interface.chosenMove(move,pl == Player1)
    var index = HouseIndex(pl,move)
    val counter = index.pull
    (1 to counter) foreach (_ => {
      index = index.next(pl)
      index.++
    })
    if (index.pl == pl && index.get == 1 && index.hn != 0 && HouseIndex(pl.other,houses + 1 - index.hn).get > 0) {
      store(pl).add(index.pull + HouseIndex(pl.other,houses + 1 - index.hn).pull)
    }
    if (!(GameBoard.p1Store + GameBoard.p2Store + GameBoard.p1Houses.values.sum + GameBoard.p2Houses.values.sum
      == 2 * GameBoard.houses * GameBoard.initSeeds)) throw new Illegal(pl,-10)
    checkEarly
    if (finish.isEmpty && finished(pl)) finish = Some(pl)
    if (finish.isEmpty && index.pl == pl && index.hn == 0)
      playerMove(pl)
  }

  case class Illegal(pl : Player, value : Int) extends Throwable

  private def finished(pl : Player) = (1 to houses).forall(i => HouseIndex(pl,i).get == 0) /*
    Some(Player2) else if ((1 to houses).forall(i => HouseIndex(Player2,i).get == 0)) Some(Player1)
  else None */
  var finish : Option[Player] = None
  private def checkEarly = if (Store1.get > GameBoard.houses * GameBoard.initSeeds) {
    println(Player1.pl.name + " wins early")
    finish = Some(Player1)
  } else if (Store2.get > GameBoard.houses * GameBoard.initSeeds) {
    println(Player2.pl.name + " wins early")
    finish = Some(Player2)
  }
  /**
    * Starts one game between (new instances of) Pl1 and Pl2.
    * @return A pair of integers representing the scores of players 1 and 2
    */
  def play : (Int,Int) = {
    interface.newGame(Player1.pl.name,Player2.pl.name,GameBoard)
    // Thread.sleep(200)
    try {
      Player1.init
    } catch {
      case e : java.util.concurrent.TimeoutException =>
        interface.timeout(true)
        return (0,1)
    }
    try {
      Player2.init
    } catch {
      case e : java.util.concurrent.TimeoutException =>
        interface.timeout(false)
        return (1,0)
    }
    try {
      while ({
        if (finish.isEmpty && finished(Player1)) finish = Some(Player1)
        finish.isEmpty
      }) {
        playerMove(Player1)
        // Thread.sleep(200)
        if (finish.isEmpty && finished(Player2)) finish = Some(Player2)
          else if (finish.isEmpty) playerMove(Player2)
        // Thread.sleep(200)

        interface.endOfRound
        // Thread.sleep(200)
      }
      val (sc1,sc2) = if (finish == Some(Player2)) {
        (Store1.sum, Store2.get)
      } else {
        (Store1.get, Store2.sum)
      }
      interface.gameResult(sc1,sc2)
      (sc1,sc2)
    } catch {
      case Illegal(Player1,_) if finished(Player2) =>
        val (sc1,sc2) = (Store1.sum,Store2.get)
        interface.gameResult(sc1,sc2)
        (sc1,sc2)
      case Illegal(Player2,_) if finished(Player1) =>
        val (sc1,sc2) = (Store1.get,Store2.sum)
        interface.gameResult(sc1,sc2)
        (sc1,sc2)
      case Illegal(Player1,i) =>
        interface.illegal(true,i,GameBoard)
        (0,Store2.get + 1)
      case Illegal(Player2,i) =>
        interface.illegal(false,i,GameBoard)
        (Store1.get + 1, 0)
    }
  }
}