package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject

case class GameOverPhase @Inject() () extends GameOverPhaseI {
  override def playCard(): GamePhase = this

  override def drawCard(): GamePhase = this

  override def nextPlayer(): GamePhase = this

  override def dealInitialCards(): GamePhase = this

  override def checkForWinner(): GamePhase = this

  override def playerSaysUno(): GamePhase = this

  override def isValidPlay: Boolean = false
}
