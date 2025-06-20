package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject

case class ReversePhase @Inject() (context: UnoPhases) extends ReversePhaseI {
  override def nextPlayer(): GamePhase = {
    context.gameState = context.gameState.copyWithIsReversed(isReversed = !context.gameState.isReversed).nextPlayer()
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def playCard(): GamePhase = this
  override def drawCard(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay: Boolean = false
}
