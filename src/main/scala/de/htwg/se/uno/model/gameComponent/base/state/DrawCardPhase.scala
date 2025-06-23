package de.htwg.se.uno.model.gameComponent.base.state

import com.google.inject.Inject
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.model.cardComponent.Card

case class DrawCardPhase @Inject() (context: UnoPhases) extends DrawCardPhaseI {
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = {
    val currentPlayer = context.gameState.players(context.gameState.currentPlayerIndex)
    val (card, updatedHand, updatedDrawPile, updatedDiscardPile) =
      context.gameState.drawCard(currentPlayer, context.gameState.drawPile, context.gameState.discardPile)

    val updatedPlayers = context.gameState.players.updated(context.gameState.currentPlayerIndex, updatedHand)
    val newState = context.gameState.copyWithPlayersAndPiles(
      players = updatedPlayers,
      drawPile = updatedDrawPile,
      discardPile = updatedDiscardPile
    )
    GameBoard.updateState(newState)
    context.gameState = newState

    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
