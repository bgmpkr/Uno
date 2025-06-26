package de.htwg.se.uno.model.cardComponent

import scala.util.Random

class CardFactory extends CardFactoryInterface {
  val colors = List("red", "blue", "green", "yellow")
  val numberRange = 0 to 9
  val actionTypes = List("draw two", "reverse", "skip")

  def createFullDeck(): List[Card] = {
    val numberCards = for {
      color <- colors
      number <- numberRange
      count = if (number == 0) 1 else 2
      _ <- 1 to count
    } yield NumberCard(color, number)

    val actionCards = for {
      color <- colors
      action <- actionTypes
      _ <- 1 to 2
    } yield ActionCard(color, action)

    val wildCards = List.fill(4)(WildCard("wild")) ++ List.fill(4)(WildCard("wild draw four"))

    val fullDeck = numberCards ++ actionCards ++ wildCards

    Random.shuffle(fullDeck.toList)
  }
}
