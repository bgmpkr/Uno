package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface

object DoubleCardRule extends StrategyPattern {
  def canPlay(selectedCards: List[Card],
              topCard: Card,
              selectedColor: Option[String],
              gameState: GameStateInterface): Boolean = {
    selectedCards.nonEmpty &&
      selectedCards.distinct.size == 1 &&
      gameState.isValidPlay(selectedCards.head, Some(gameState.discardPile.head), gameState.selectedColor)
  }

  def isMultiPlayAllowed: Boolean = true
}

