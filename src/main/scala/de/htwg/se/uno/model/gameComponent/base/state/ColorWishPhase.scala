package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card

case class ColorWishPhase @Inject() (context: UnoPhases) extends ColorWishPhaseI {
  override def playCard(card: Card): GamePhase = {
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def drawCard(): GamePhase = this
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
