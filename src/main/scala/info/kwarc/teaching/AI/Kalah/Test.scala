package info.kwarc.teaching.AI.Kalah

/**
  * Created by jazzpirate on 08.11.16.
  */
object Test {
  def main(args: Array[String]): Unit = {
    val game = new Game(new HumanPlayer("Pl1"),new HumanPlayer("Pl2"))(6,6)
    val (sc1,sc2) = game.play
    println("Score Player 1: " + sc1)
    println("Score Player 2: " + sc2)
    println(if (sc1 > sc2) "Player 1 wins!" else if (sc2 > sc1) "Player 2 wins!" else "Draw!")
  }
}
