package de.htwg.se.uno.model.gameComponent.base.state

trait CheckWinnerPhaseI extends GamePhase {
  def checkForWinner(): GamePhase
}

trait ColorWishPhaseI extends GamePhase {
  def playCard(): GamePhase
}

trait DrawCardPhaseI extends GamePhase {
  def drawCard(): GamePhase
}

trait GameOverPhaseI extends GamePhase

trait PlayCardPhaseI extends GamePhase {
  def playCard(): GamePhase
  def isValidPlay: Boolean
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
