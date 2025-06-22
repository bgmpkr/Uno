package de.htwg.se.uno.model.gameComponent.base.state

case class SkipPhase (context: UnoPhases) extends SkipPhaseI {
  override def nextPlayer(): GamePhase = {
    context.gameState = context.gameState.nextPlayer().nextPlayer()
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def playCard(): GamePhase = this
  override def drawCard(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay: Boolean = false
}
