package de.htwg.se.uno.controller.controllerComponent

import de.htwg.se.uno.controller.controllerComponent.base.Controller
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.strategy.StrategyPattern
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.{Command, Observable}

import scala.util.Try

trait ControllerInterface extends Observable {
  val fullDeck: List[Card]
  var strategyPattern: StrategyPattern
  val allowDoubleCards: Boolean = false
  def resetUndoRedo(): Unit
  def setStrategyPattern(strategy: StrategyPattern): Unit
  def playTurn(player: PlayerHand, topCard: Card): Unit
  def setGameState(newState: GameStateInterface): Unit
  def gameState: Try[GameStateInterface]
  def startGame(players: Int, cardsPerPlayer: Int): Unit
  def updateState(newState: GameStateInterface): Unit
  def initGame(state: GameStateInterface): Unit
  def undoCommand(): Unit
  def redoCommand(): Unit
  def executeCommand(cmd: Command): Unit
  def checkForWinner(): Option[Int]
  def input(command: String): Unit
  def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean
}
