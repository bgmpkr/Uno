package de.htwg.se.uno.model.cardComponent

trait CardFactoryInterface {
  def createFullDeck(): List[Card]
}
