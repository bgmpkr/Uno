package de.htwg.se.uno.model.gameComponent.base.state

trait GamePhase {
  def nextPlayer(): GamePhase
  def dealInitialCards(): GamePhase
  def checkForWinner(): GamePhase
  def playerSaysUno(): GamePhase
  def drawCard(): GamePhase
  def playCard(): GamePhase
  def isValidPlay: Boolean
}

object GamePhase {
  def withName(name: String): GamePhase = name match {
    case "CheckWinnerPhase" => CheckWinnerPhase.asInstanceOf[GamePhase]
    case "ColorWishPhase" => ColorWishPhase.asInstanceOf[GamePhase]
    case "DrawCardPhase" => DrawCardPhase.asInstanceOf[GamePhase]
    case "GameOverPhase" => GameOverPhase.asInstanceOf[GamePhase]
    case "PlayCardPhase" => PlayCardPhase.asInstanceOf[GamePhase]
    case "PlayerTurnPhase" => PlayerTurnPhase.asInstanceOf[GamePhase]
    case "ReversePhase" => ReversePhase.asInstanceOf[GamePhase]
    case "SkipPhase" => SkipPhase.asInstanceOf[GamePhase]
    case "StartPhase" => StartPhase.asInstanceOf[GamePhase]
    case "UnoCalledPhase" => UnoCalledPhase.asInstanceOf[GamePhase]
  }
}
