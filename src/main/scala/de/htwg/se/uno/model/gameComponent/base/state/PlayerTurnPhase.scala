package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card

case class PlayerTurnPhase @Inject() (context: UnoPhases) extends PlayerTurnPhaseI {
  override def nextPlayer(): GamePhase = {
    context.gameState = context.gameState.nextPlayer()
    context.state
  }
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
