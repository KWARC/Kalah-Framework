package info.kwarc.teaching.AI.Kalah

import info.kwarc.teaching.AI.Kalah.utils._

object Test {
  def main(args: Array[String]): Unit = {
    val file = File("/home/jazzpirate/work/scores.txt")

    //val int = new Fancy.Fan
    val tn = new Tournament {
      val players: List[String] = List("R1", "R2", "R3"/*, "Jazzpirate"*/)
      val interface = Terminal

      def getPlayer(s: String): Agent = s match {
        case "R1" => new RandomPlayer("R1")
        case "R2" => new RandomPlayer("R2")
        case "R3" => new RandomPlayer("R3")
        // case "Jazzpirate" => new Jazzpirate
        case _ => throw new Exception("No player with name " + s + " found!")
      }
    }
    //println(tn.readFromFile(file))
    tn.run(6,6)
    // new Game(new Jazzpirate,new RandomPlayer("Dennis"),Terminal)(12,12).play
    // tn.saveToFile(file)
  }
}

