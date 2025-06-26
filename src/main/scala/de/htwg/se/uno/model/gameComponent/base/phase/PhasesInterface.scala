package de.htwg.se.uno.model.gameComponent.base.phase

import de.htwg.se.uno.model.cardComponent.Card

trait CheckWinnerPhaseI extends GamePhase {
  def checkForWinner(): GamePhase
}

trait ColorWishPhaseI extends GamePhase {
  def playCard(card: Card): GamePhase
}

trait DrawCardPhaseI extends GamePhase {
  def drawCard(): GamePhase
}

trait GameOverPhaseI extends GamePhase

trait PlayCardPhaseI extends GamePhase {
  def playCard(card: Card): GamePhase
  def isValidPlay(card: Card): Boolean
}

trait PlayerTurnPhaseI extends GamePhase {
  def nextPlayer(): GamePhase
}

trait ReversePhaseI extends GamePhase {
  def nextPlayer(): GamePhase
}

trait SkipPhaseI extends GamePhase {
  def nextPlayer(): GamePhase
}

trait StartPhaseI extends GamePhase {
  def dealInitialCards(): GamePhase
}

trait UnoCalledPhaseI extends GamePhase {
  def playerSaysUno(): GamePhase
}
