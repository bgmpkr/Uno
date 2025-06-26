package de.htwg.se.uno.model.cardComponent

import de.htwg.se.uno.model.cardComponent.Card

trait CardFactoryInterface {
  def createFullDeck(): List[Card]
}
