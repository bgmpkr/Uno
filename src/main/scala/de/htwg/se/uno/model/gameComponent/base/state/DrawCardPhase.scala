package de.htwg.se.uno.model.gameComponent.base.state

case class DrawCardPhase (context: UnoPhases) extends DrawCardPhaseI {
  override def playCard(): GamePhase = this
  override def drawCard(): GamePhase = {
    val currentPlayer = context.gameState.players(context.gameState.currentPlayerIndex)
    val (card, updatedHand, updatedDrawPile, updatedDiscardPile) =
      context.gameState.drawCard(currentPlayer, context.gameState.drawPile, context.gameState.discardPile)

    val updatedPlayers = context.gameState.players.updated(context.gameState.currentPlayerIndex, updatedHand)
    context.gameState = context.gameState.copyWithPlayersAndPiles(
      players = updatedPlayers,
      drawPile = updatedDrawPile,
      discardPile = updatedDiscardPile
    )
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay: Boolean = false
}
