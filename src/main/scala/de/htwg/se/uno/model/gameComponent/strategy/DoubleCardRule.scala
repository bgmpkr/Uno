package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card

object DoubleCardRule extends StrategyPattern {
  override def canPlay(cardsInHand: List[Card], topCard: Card): List[Card] = {
    val matching = cardsInHand.filter(_.matches(topCard))
    if (matching.size >= 2 && matching.groupBy(identity).exists(_._2.size >= 2)) {
      val doubleCard = matching.groupBy(identity).find(_._2.size >= 2).get._2.take(2)
      doubleCard
    } else matching.take(1)
  }

  def isMultiPlayAllowed: Boolean = true
}

