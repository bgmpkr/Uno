package de.htwg.se.uno.model.gameComponent.base.phase

import com.google.inject.Inject
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.Card

case class StartPhase @Inject() (context: UnoPhases) extends StartPhaseI {
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = this
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = {
    context.gameState = context.gameState.dealInitialCards(7)
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
