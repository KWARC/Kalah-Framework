package info.kwarc.teaching.AI.Kalah

import info.kwarc.teaching.AI.Kalah.utils._
// import WS1617.agents._

object Test {
  def main(args: Array[String]): Unit = {

    val int = new Fancy.FancyInterface(12)

    new Game(new HumanPlayer("Hans"),new RandomPlayer("Hurtz"),int + Terminal)(12,12).play
  }
}
