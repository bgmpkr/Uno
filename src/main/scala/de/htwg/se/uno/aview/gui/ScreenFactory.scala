package de.htwg.se.uno.aview.gui

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.{Card, CardFactoryInterface}

class ScreenFactory @Inject() (cardFactory: CardFactoryInterface) {
  val allCards: List[Card] = cardFactory.createFullDeck()
}
