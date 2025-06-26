package de.htwg.se.uno.model.gameComponent.base.phase

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card

case class CheckWinnerPhase @Inject() (context: UnoPhases) extends CheckWinnerPhaseI {
  override def checkForWinner(): GamePhase = {
    context.gameState.checkForWinner() match {
      case Some(_) =>
        context.setState(GameOverPhase())
      case None =>
        context.setState(PlayerTurnPhase(context))
    }
    context.state
  }
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = this
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}