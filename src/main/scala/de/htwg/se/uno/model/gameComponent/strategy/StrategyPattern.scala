package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface

trait StrategyPattern {
  def canPlay(selectedCards: List[Card], topCard: Card, selectedColor: Option[String], gameState: GameStateInterface):
  Boolean
  def isMultiPlayAllowed: Boolean
}
