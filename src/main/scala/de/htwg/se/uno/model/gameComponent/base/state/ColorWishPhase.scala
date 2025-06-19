package de.htwg.se.uno.model.gameComponent.base.state

case class ColorWishPhase(context: UnoPhases) extends GamePhase {
  override def playCard(): GamePhase = {
    context.setState(PlayerTurnPhase(context))
    context.state
  }
  override def drawCard(): GamePhase = this
  override def nextPlayer(): GamePhase = this
  override def dealInitialCards(): GamePhase = this
  override def checkForWinner(): GamePhase = this
  override def playerSaysUno(): GamePhase = this
  override def isValidPlay: Boolean = false
}
