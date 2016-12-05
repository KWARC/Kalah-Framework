package info.kwarc.teaching.AI.Kalah

import scala.collection.mutable

/**
  * Created by jazzpirate on 08.11.16.
  */
object Test {
  def main(args: Array[String]): Unit = {
    /*
    val game = new Game(new RandomPlayer,new RandomPlayer)(6,6)
    val (sc1,sc2) = game.play()
    println("Score Player 1: " + sc1)
    */
    val tn = new Tournament(List(new RandomPlayer("R1"), new RandomPlayer("R2"), new RandomPlayer("R3")))
    val ret = tn.run(6,6)
    ret.indices.foreach(i => println(i + ": " + ret(i)._1.name + "(" + ret(i)._2 + ")"))
  }
}

class Tournament(players : List[Agent]) {

  val scores = mutable.HashMap(players.map((_,0)):_*)

  def run(houses: Int, seeds : Int) = {
    players foreach (p => {
      players foreach (q => if (p!=q) {
        println(p.name + " vs. " + q.name)
        val result = (new Game(p,q)(houses,seeds)).play()
        if (result._1 > result._2) {
          println(p.name + " wins!")
          scores(p)+= 1
        }
        else if (result._2 > result._1) {
          println(q.name + " wins!")
          scores(q) += 1
        }
      })
    })
    scores.toList.sortBy(_._2)
  }
}