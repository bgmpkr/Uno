package de.htwg.se.uno.model.cardComponent

import de.htwg.se.uno.model.cardComponent.Card

trait CardFactory {
  def createFullDeck(): List[Card]
}
