package de.htwg.se.uno.util

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState

object Default {
  given ControllerInterface = GameBoard
  given GameStateInterface = GameState(players = List.empty,
    currentPlayerIndex = 0,
    allCards = List.empty,
    isReversed = false,
    discardPile = List.empty,
    drawPile = List.empty,
    selectedColor = None,
    currentPhase = None)
}
