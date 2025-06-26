package de.htwg.se.uno.model.gameComponent.base.phase

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
