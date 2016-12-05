package info.kwarc.teaching.AI.Kalah

import scala.collection.mutable

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

object Tournament {
  val players : List[Agent] = ???

  val scores = mutable.HashMap(players.map((_,0)):_*)

  def run(houses: Int, seeds : Int) {
    players foreach (p => {
      players foreach (q => if (p!=q) {
        val result = (new Game(p,q)(houses,seeds)).play
        scores(p) += result._1
        scores(q) += result._2
      })
    })
  }
}