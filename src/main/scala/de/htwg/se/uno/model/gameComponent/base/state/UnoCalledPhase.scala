package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card

case class UnoCalledPhase @Inject() (context: UnoPhases) extends UnoCalledPhaseI {
  override def playerSaysUno(): GamePhase = {
    val idx = context.gameState.currentPlayerIndex
    context.gameState = context.gameState.playerSaysUno(idx)
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = this
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
