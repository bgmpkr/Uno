package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card

trait StrategyPattern {
  def canPlay(cardsInHand: List[Card], topCard: Card): List[Card]
  def isMultiPlayAllowed: Boolean
}
