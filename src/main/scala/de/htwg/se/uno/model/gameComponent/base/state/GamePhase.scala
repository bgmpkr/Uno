package de.htwg.se.uno.model.gameComponent.base.state

import de.htwg.se.uno.model.cardComponent.Card

trait GamePhase {
  def nextPlayer(): GamePhase
  def dealInitialCards(): GamePhase
  def checkForWinner(): GamePhase
  def playerSaysUno(): GamePhase
  def drawCard(): GamePhase
  def playCard(card: Card): GamePhase
  def isValidPlay(card: Card): Boolean
}
