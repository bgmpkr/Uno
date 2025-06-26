package de.htwg.se.uno.model.gameComponent.base.phase

import com.google.inject.Inject
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.playerComponent.PlayerHand

case class PlayerTurnPhase @Inject() (context: UnoPhases) extends PlayerTurnPhaseI {
  override def nextPlayer(): GamePhase = {
    context.gameState = context.gameState.nextPlayer()
    context.state
  }
  override def playCard(card: Card): GamePhase = this
  override def drawCard(): GamePhase = {
    val gameState = context.gameState
    val discardPile = gameState.discardPile
    val drawPile = gameState.drawPile
    if (drawPile.nonEmpty) {
      val card = drawPile.head
      val updatedHand = gameState.players(gameState.currentPlayerIndex).cards :+ card
      val updatedPlayers = gameState.players.updated(gameState.currentPlayerIndex, PlayerHand(updatedHand))

      val updatedGameState = gameState.copyWithPlayersAndPiles(
        players = updatedPlayers,
        drawPile = drawPile.tail,
        discardPile = discardPile.tail
      )
      context.gameState = updatedGameState

      println(s"Player drew: $card")
    } else {
      println("Draw pile is empty.")
    }
    this
  }
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay(card: Card): Boolean = false
}
