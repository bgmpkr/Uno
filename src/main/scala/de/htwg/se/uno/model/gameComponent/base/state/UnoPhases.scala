package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface

class UnoPhases @Inject() (var gameState: GameStateInterface) {
  private var currentState: GamePhase = StartPhase(this)

  def init(): Unit = {
    currentState = StartPhase(this)
  }

  def setState(state: GamePhase): Unit = currentState = state
  def state: GamePhase = currentState

  def playCard(card: Card): Unit = currentState = currentState.playCard(card: Card)
  def drawCard(): Unit = currentState = currentState.drawCard()
  def nextPlayer(): Unit = currentState = currentState.nextPlayer()
  def dealInitialCards(): Unit = currentState = currentState.dealInitialCards()
  def checkForWinner(): Unit = currentState = currentState.checkForWinner()
  def playerSaysUno(): Unit = currentState = currentState.playerSaysUno()
  def tryPlayCard(card: Card): Unit = {
    if (currentState.isValidPlay(card)) currentState = currentState.playCard(card: Card)
    else println("Invalid play.")
  }
  
  var selectedColor: Option[String] = None
  def setSelectedColor(color: String): Unit = selectedColor = Some(color)
  def updateGameState(newState: GameStateInterface): Unit = {
    this.gameState = newState

    if (newState.players.exists(p => p.cards.isEmpty && p.hasSaidUno)) {
      currentState = GameOverPhase()
    } else if (currentState == null) {
      currentState = StartPhase(this)
    }
  }
}

