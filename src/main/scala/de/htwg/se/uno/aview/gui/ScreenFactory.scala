package de.htwg.se.uno.aview.gui

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.{Card, CardFactory}

class ScreenFactory @Inject() (cardFactory: CardFactory) {
  val allCards: List[Card] = cardFactory.createFullDeck()
}
