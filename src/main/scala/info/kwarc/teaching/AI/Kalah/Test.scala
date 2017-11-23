package info.kwarc.teaching.AI.Kalah

import info.kwarc.teaching.AI.Kalah.utils._
import WS1617.agents._
import collection.JavaConverters._

object Test {
  def main(args: Array[String]): Unit = {

    val file = File("/home/jazzpirate/scores.txt")

    val allplayers = List("Random1", "Random2", "Random3",
      "Jazzpirate",
      "AgentOrange",
      "AssKickinAgent",
      "AgentSpaghetti",
      "AgentSmith",
      "HadduMoehre",
      "Kalahnator3000",
      "Joshua",
      "LynnAdam",
      "Koalah",
      "MPLAgent", //TODO: set team name
      "MrSmith",
      "FinalDude",
      "PRMHAgent",
      "ProjectSkynetSearch",
      "OneManShow",
      "TinyRick",
      "Granny4TheWin",
      "DJSuperStarSolverMasterMind",
      "ChiefZed",
      "LuckyPlayer",
      "FrankThePug"
    )
    val int = new Fancy.FancyInterface(26)
    def allPlayers(s: String): Agent = s match {
      case "Random1" => new RandomPlayer("Random1")
      case "Random2" => new RandomPlayer("Random2")
      case "Random3" => new RandomPlayer("Random3")
      case "Jazzpirate" => new Jazzpirate
      case "AgentOrange" => new AgentOrange
      case "AssKickinAgent" => new AssKickinAgent
      case "AgentSpaghetti" => new AgentSpaghetti
      case "AgentSmith" => new AgentSmith
      case "HadduMoehre" => new HadduMoehre
      case "Kalahnator3000" => new Kalahnator3000
      case "Joshua" => new Joshua
      case "LynnAdam" => new LynnAdamPlayer("LynnAdam")
      case "Koalah" => new Koalah
      case "MPLAgent" => new MPLAgent
      case "MrSmith" => new MrSmith
      case "FinalDude" => new FinalDude
      case "PRMHAgent" => new PRMHAgent("PRMHAgent")
      case "ProjectSkynetSearch" => new ProjectSkynetSearch
      case "OneManShow" => new OneManShow
      case "TinyRick" => new TinyRick
      case "Granny4TheWin" => new GrannyPlayer
      case "DJSuperStarSolverMasterMind" => new DJSuperStarSolverMasterMind
      case "ChiefZed" => new ChiefZed
      case "LuckyPlayer" => new LuckyPlayer("LuckyPlayer")
      case "FrankThePug" => new FrankThePug
      case _ => throw new Exception("No player with name " + s + " found!")
    }
    case class Round(pls : String*)(logf : Option[File] = None) extends Tournament {
      def getPlayer(s : String) = allPlayers(s)
      val players = pls.toList
      int.slow = false
      val interface = if (logf.isDefined) Logger(logf.get) + int else int
      def loadandsave(h : Int, s : Int, slow : Boolean = false) = {
        int.slow = slow
        readFromFile(file)
        run(h,s)
        saveToFile(file)
      }
    }
    allplayers foreach (p => println({val pl = allPlayers(p); pl.name + ": " + pl.students.asScala.mkString(", ")}))


    // Round(allplayers:_*)(Some(File("/home/jazzpirate/log44.txt"))).loadandsave(4,4)
    //Thread.sleep(500)
    //Round(allplayers:_*)(Some(File("/home/jazzpirate/log66.txt"))).loadandsave(6,6)
    //Thread.sleep(500)
    //val r = Round(allplayers:_*)(Some(File("/home/jazzpirate/log1010.txt")))//.loadandsave(8,8)
    //r.loglist = File.read(File("/home/jazzpirate/log1010.txt")).split("\n").toList
    //r.loadandsave(10,10)
    Thread.sleep(500)
    //Round(allplayers:_*)(Some(File("/home/jazzpirate/log1010.txt"))).loadandsave(10,10)


    //tn.readFromFile(file)
    //tn.run(5,5)
    //new Game(new Jazzpirate, new MPLAgent, Terminal + int)(4,4).play
    //tn.saveToFile(file)
  }
}