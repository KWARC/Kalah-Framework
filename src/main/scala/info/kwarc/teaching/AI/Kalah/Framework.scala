package info.kwarc.teaching.AI.Kalah

import java.io._

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
    val p1Houses = mutable.HashMap.empty[Int,Int]
    val p2Houses = mutable.HashMap.empty[Int,Int]
    (1 to houses) foreach (i => {
      p1Houses(i) = initSeeds
      p2Houses(i) = initSeeds
    })
    var p1Store = 0
    var p2Store = 0

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

  private def playerMove(pl : Player) : Unit = {
    interface.playerMove(pl == Player1)
    val move = pl.pl match {
      case hp : HumanPlayer => pl.move
      case _ => try {
        AgentAction.move(pl.pl,5000)
      } catch {
        case e : java.util.concurrent.TimeoutException =>
          pl.pl.timeoutMove
      }
    }
    if (!(move >= 1 && move <= houses && HouseIndex(pl,move).get > 0)) throw Illegal(pl)
    interface.chosenMove(move,pl == Player1)
    var index = HouseIndex(pl,move)
    val counter = index.pull
    (1 to counter) foreach (_ => {
      index = index.next(pl)
      index.++
    })
    if (index.pl == pl && index.get == 1 && index.hn != 0 && HouseIndex(pl.other,houses + 1 - index.hn).get != 0) {
      store(pl).add(index.pull + HouseIndex(pl.other,houses + 1 - index.hn).pull)
    }
    if (index.pl == pl && index.hn == 0 && (1 to houses).exists(i => HouseIndex(pl,i).get > 0))
      playerMove(pl)
  }

  case class Illegal(pl : Player) extends Throwable

  private def finished : Option[Player] = if ((1 to houses).forall(i => HouseIndex(Player1,i).get == 0))
    Some(Player2) else if ((1 to houses).forall(i => HouseIndex(Player2,i).get == 0)) Some(Player1)
  else None
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
      while (finished.isEmpty) {
        playerMove(Player1)
        // Thread.sleep(200)
        if (finished.isEmpty) playerMove(Player2)
        // Thread.sleep(200)
        interface.endOfRound
        // Thread.sleep(200)
      }
      val (sc1,sc2) = if (finished == Some(Player1)) {
        val (r1,r2) = (Store1.sum, Store2.get)
        if (r1 == r2) (r1 +1,r2) else (r1,r2)
      } else {
        val (r1,r2) = (Store1.get, Store2.sum)
        if (r1 == r2) (r1,r2 + 1) else (r1,r2)
      }
      interface.gameResult(sc1,sc2)
      (sc1,sc2)
    } catch {
      case Illegal(Player1) =>
        interface.illegal(true)
        (0,Store2.get + 1)
      case Illegal(Player2) =>
        interface.illegal(false)
        (Store1.get + 1, 0)
    }
  }
}

abstract class Tournament {
  val interface : Interface
  val players : List[String] // = List("R1","R2","R3","Jazzpirate")
  def getPlayer(s : String) : Agent /* = s match {
    case "R1" => new RandomPlayer ("R1")
    case "R2" => new RandomPlayer ("R2")
    case "R3" => new RandomPlayer ("R3")
    case "Jazzpirate" => new Jazzpirate
    case _ => throw new Exception("No player with name " + s + " found!")
  } */

  lazy val scores = mutable.HashMap(players.map(p => (p,0)):_*)

  def run(houses: Int, seeds : Int, showboard : Boolean = false) = {
    players foreach (p => {
      players foreach (q => if (p!=q) {
        val result = (new Game(getPlayer(p),getPlayer(q),interface)(houses,seeds)).play
        if (result._1 > result._2) {
          scores(p)+= houses
        }
        else if (result._2 > result._1) {
          scores(q) += houses
        }
      })
    })
    val ret = scores.toList.sortBy(_._2).reverse
    interface.scoreboard(ret)
    ret
  }
  import utils._

  def readFromFile(f : File) = {
    scores.clear()
    val scs = File.read(f).split("\n").filterNot(_.isEmpty).map(_.split(" -score- "))
    scs foreach (l => scores(l.head) = l.tail.head.toInt)
    interface.scoreboard(scores.toList.sortBy(_._2).reverse)
  }
  def saveToFile(f : File) = {
    val scs = scores.map(p => p._1 + " -score- " + p._2).mkString("\n")
    File.write(f,scs)
  }

}

object utils {

  case class File(toJava: java.io.File) {
    /** resolves an absolute or relative path string against this */
    def resolve(s: String): File = {
      val sf = new java.io.File(s)
      val newfile = if (sf.isAbsolute)
        sf
      else
        new java.io.File(toJava, s)
      File(newfile.getCanonicalPath)
    }

    def canonical = File(toJava.getCanonicalFile)

    /** appends one path segment */
    def /(s: String): File = File(new java.io.File(toJava, s))

    /** appends a list of path segments */
    def /(ss: List[String]): File = ss.foldLeft(this) { case (sofar, next) => sofar / next }

    /** appends a relative path */
    def /(ss: FilePath): File = this / ss.segments

    /** parent directory */
    def up: File = {
      val par = Option(toJava.getParentFile)
      if (par.isEmpty) this else File(par.get)
    }

    def isRoot = up == this

    /** file name */
    def name: String = toJava.getName

    /** segments as a FilePath
      */
    def toFilePath = FilePath(segments)

    /** the list of file/directory/volume label names making up this file path
      * an absolute Unix paths begin with an empty segment
      */
    def segments: List[String] = {
      val name = toJava.getName
      val par = Option(toJava.getParentFile)
      if (par.isEmpty)
        if (name.isEmpty) if (toString.nonEmpty) List(toString.init) else Nil
        else List(name) // name == "" iff this File is a root
      else
        File(par.get).segments ::: List(name)
    }

    def isAbsolute: Boolean = toJava.isAbsolute

    override def toString = toJava.toString

    /** @return the last file extension (if any) */
    def getExtension: Option[String] = {
      val name = toJava.getName
      val posOfDot = name.lastIndexOf(".")
      if (posOfDot == -1) None else Some(name.substring(posOfDot + 1))
    }

    /** sets the file extension (replaces existing extension, if any) */
    def setExtension(ext: String): File = getExtension match {
      case None => File(toString + "." + ext)
      case Some(s) => File(toString.substring(0, toString.length - s.length) + ext)
    }

    /** appends a file extension (possibly resulting in multiple extensions) */
    def addExtension(ext: String): File = getExtension match {
      case None => setExtension(ext)
      case Some(e) => setExtension(e + "." + ext)
    }

    /** removes the last file extension (if any) */
    def stripExtension: File = getExtension match {
      case None => this
      case Some(s) => File(toString.substring(0, toString.length - s.length - 1))
    }

    /** @return children of this directory */
    def children: List[File] = if (toJava.isFile) Nil else toJava.list.toList.sorted.map(this / _)

    /** @return subdirectories of this directory */
    def subdirs: List[File] = children.filter(_.toJava.isDirectory)

    /** @return all files in this directory or any subdirectory */
    def descendants: List[File] = children.flatMap {c =>
      if (c.isDirectory) c.descendants else List(c)
    }

    /** @return true if that begins with this */
    def <=(that: File) = that.segments.startsWith(segments)

    /** delete this, recursively if directory */
    def deleteDir {
      children foreach {c =>
        if (c.isDirectory) c.deleteDir
        else c.toJava.delete
      }
      toJava.delete
    }
  }

  /** a relative file path usually within an archive below a dimension */
  case class FilePath(segments: List[String]) {
    def toFile = File(toString)

    def name: String = if (segments.nonEmpty) segments.last else ""

    def dirPath = FilePath(if (segments.nonEmpty) segments.init else Nil)

    /** append a segment */
    def /(s: String): FilePath = FilePath(segments ::: List(s))

    override def toString: String = segments.mkString("/")

    def getExtension = toFile.getExtension
    def setExtension(e: String) = toFile.setExtension(e).toFilePath
    def stripExtension = toFile.stripExtension.toFilePath
  }

  object EmptyPath extends FilePath(Nil)

  object FilePath {
    def apply(s:String): FilePath = FilePath(List(s))
    implicit def filePathToList(fp: FilePath) = fp.segments
    implicit def listToFilePath(l: List[String]) = FilePath(l)

    def getall(f : File) : List[File] = rec(List(f))

    private def rec(list : List[File]) : List[File] = list.flatMap(f => if (f.isDirectory) rec(f.children) else List(f))
  }

  /** MMT's default way to write to files; uses buffering, UTF-8, and \n */
  class StandardPrintWriter(f: File) extends
    OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f.toJava)),
      java.nio.charset.Charset.forName("UTF-8")) {
    def println(s: String): Unit = {
      write(s + "\n")
    }
  }

  /** This defines some very useful methods to interact with text files at a high abstraction level. */
  object File {
    /** constructs a File from a string, using the java.io.File parser */
    def apply(s: String): File = File(new java.io.File(s))

    def Writer(f: File): StandardPrintWriter = {
      f.up.toJava.mkdirs
      new StandardPrintWriter(f)
    }

    /**
      * convenience method for writing a string into a file
      *
      * overwrites existing files, creates directories if necessary
      * @param f the target file
      * @param strings the content to write
      */
    def write(f: File, strings: String*) {
      val fw = Writer(f)
      strings.foreach { s => fw.write(s) }
      fw.close
    }


    /**
      * streams a list-like object to a file
      * @param f the file to write to
      * @param begin initial text
      * @param sep text in between elements
      * @param end terminal text
      * @param work bind a variable "write" and call it to write into the file
      * example: (l: List[Node]) => stream(f, "<root>", "\n", "</root>"){out => l map {a => out(a.toString)}}
      */
    def stream(f: File, begin: String = "", sep: String = "", end: String="")(work: (String => Unit) => Unit) = {
      val fw = Writer(f)
      fw.write(begin)
      var writeSep = false
      def out(s: String) {
        if (writeSep)
          fw.write(sep)
        else
          writeSep = true
        fw.write(s)
      }
      try {
        work(out)
        fw.write(end)
      } finally {
        fw.close
      }
    }

    /**
      * convenience method for writing a list of lines into a file
      *
      * overwrites existing files, creates directories if necessary
      * @param f the target file
      * @param lines the lines (without line terminator - will be chosen by Java and appended)
      */
    def WriteLineWise(f: File, lines: List[String]) {
      val fw = Writer(f)
      lines.foreach {l =>
        fw.println(l)
      }
      fw.close
    }

    /**
      * convenience method for reading a file into a string
      *
      * @param f the source file
      * @return s the file content (line terminators are \n)
      */
    def read(f: File): String = {
      val s = new StringBuilder
      ReadLineWise(f) {l => s.append(l + "\n")}
      s.result
    }

    /** convenience method to obtain a typical (buffered, UTF-8) reader for a file */
    def Reader(f: File): BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f.toJava),
      java.nio.charset.Charset.forName("UTF-8")))

    /** convenience method to read a file line by line
      * @param f the file
      * @param proc a function applied to every line (without line terminator)
      */
    def ReadLineWise(f: File)(proc: String => Unit) {
      val r = Reader(f)
      var line: Option[String] = None
      try {
        while ( {
          line = Option(r.readLine)
          line.isDefined
        })
          proc(line.get)
      } finally {
        r.close
      }
    }

    def readProperties(manifest: File): mutable.Map[String, String] = {
      val properties = new scala.collection.mutable.ListMap[String, String]
      File.ReadLineWise(manifest) { case line =>
        // usually continuation lines start with a space but we ignore those
        val tline = line.trim
        if (!tline.startsWith("//")) {
          val p = tline.indexOf(":")
          if (p > 0) {
            // make sure line contains colon and the key is non-empty
            val key = tline.substring(0, p).trim
            val value = tline.substring(p + 1).trim
            properties(key) = properties.get(key) match {
              case None => value
              case Some(old) => old + " " + value
            }
          }
        }
      }
      properties
    }

    /** copies a file */
    def copy(from: File, to: File, replace: Boolean): Boolean = {
      if (!from.exists || (to.exists && !replace)) {
        false
      } else {
        to.getParentFile.mkdirs
        val opt = if (replace) List(java.nio.file.StandardCopyOption.REPLACE_EXISTING) else Nil
        java.nio.file.Files.copy(from.toPath, to.toPath, opt:_*)
        true
      }
    }

    /** implicit conversion Java <-> Scala */
    implicit def scala2Java(file: File): java.io.File = file.toJava

    /** implicit conversion Java <-> Scala */
    implicit def java2Scala(file: java.io.File): File = File(file)
  }
}