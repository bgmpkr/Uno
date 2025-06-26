package de.htwg.se.uno.model.gameComponent.strategy

import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface

object StandardRule extends StrategyPattern {
  override def canPlay(
                        selectedCards: List[Card],
                        topCard: Card,
                        selectedColor: Option[String],
                        gameState: GameStateInterface
                      ): Boolean = {
    selectedCards.length == 1 &&
      gameState.isValidPlay(selectedCards.head, Some(gameState.discardPile.head), gameState.selectedColor)
  }

  override def isMultiPlayAllowed: Boolean = false
}


