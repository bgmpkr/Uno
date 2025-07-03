package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card

object StandardRule extends StrategyPattern {
  override def canPlay(cardsInHand: List[Card], topCard: Card): List[Card] = {
    cardsInHand.find(_.matches(topCard)).toList
  }

  override def isMultiPlayAllowed: Boolean = false
}


