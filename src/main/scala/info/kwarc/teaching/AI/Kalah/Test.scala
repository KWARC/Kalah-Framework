package info.kwarc.teaching.AI.Kalah

// import info.kwarc.teaching.AI.Kalah.WS1617.agents.Jazzpirate
import info.kwarc.teaching.AI.Kalah.utils._

object Test {
  def main(args: Array[String]): Unit = {
    val file = File("/home/jazzpirate/work/scores.txt")
    /*
    val game = new Game(new RandomPlayer,new RandomPlayer)(6,6)
    val (sc1,sc2) = game.play()
    println("Score Player 1: " + sc1)
    */
    val tn = new Tournament {
      val players: List[String] = List("R1", "R2", "R3"/*, "Jazzpirate"*/)

      def getPlayer(s: String): Agent = s match {
        case "R1" => new RandomPlayer("R1")
        case "R2" => new RandomPlayer("R2")
        case "R3" => new RandomPlayer("R3")
        // case "Jazzpirate" => new Jazzpirate
        case _ => throw new Exception("No player with name " + s + " found!")
      }
    }
    //println(tn.readFromFile(file))
    tn.run(12,12)
    //new Game(new Jazzpirate,new HumanPlayer("Dennis"))(12,12).play()
    println(tn.scoreboard)
    //tn.saveToFile(file)
  }
}
