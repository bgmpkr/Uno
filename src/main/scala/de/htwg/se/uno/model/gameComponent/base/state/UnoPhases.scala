package de.htwg.se.uno.model.gameComponent.base.state

import de.htwg.se.uno.model.gameComponent.GameStateInterface

class UnoPhases(var gameState: GameStateInterface) {
  private var currentState: GamePhase = _

  def init(): Unit = {
    currentState = StartPhase(this)
  }

  def setState(state: GamePhase): Unit = currentState = state
  def state: GamePhase = currentState
  def playCard(): Unit = currentState = currentState.playCard()
  def drawCard(): Unit = currentState = currentState.drawCard()
  def nextPlayer(): Unit = currentState = currentState.nextPlayer()
  def dealInitialCards(): Unit = currentState = currentState.dealInitialCards()
  def checkForWinner(): Unit = currentState = currentState.checkForWinner()
  def playerSaysUno(): Unit = currentState = currentState.playerSaysUno()
  def tryPlayCard(): Unit = {
    if (currentState.isValidPlay) currentState = currentState.playCard()
    else println("Invalid play.")
  }
  
  var selectedColor: Option[String] = None
  def setSelectedColor(color: String): Unit = selectedColor = Some(color)
}

