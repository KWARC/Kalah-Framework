package info.kwarc.teaching.AI.Kalah

import info.kwarc.teaching.AI.Kalah.utils._
// import WS1617.agents._

object Test {
  def main(args: Array[String]): Unit = {
    val file = File("/home/jazzpirate/work/scores.txt")

    val int = new Fancy.FancyInterface
    val tn = new Tournament {
      val players: List[String] = List("R1", "R2", "R3"/*, "Jazzpirate"*/)
      val interface = int

      def getPlayer(s: String): Agent = s match {
        case "R1" => new RandomPlayer("R1")
        case "R2" => new RandomPlayer("R2")
        case "R3" => new RandomPlayer("R3")
          /*
        case "Jazzpirate" =>
          int.slow = true
          new Jazzpirate
          */
        case _ => throw new Exception("No player with name " + s + " found!")
      }
    }
    int.slow = false
    //tn.readFromFile(file)
    tn.run(6,6)
    // new Game(new Jazzpirate,new RandomPlayer("Dennis"),Terminal)(12,12).play
    //tn.saveToFile(file)
  }
}

