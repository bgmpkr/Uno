package de.htwg.se.uno.controller.controllerComponent.mock

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.strategy.{StandardRule, StrategyPattern}
import de.htwg.se.uno.util.{Command, Observer}

import scala.util.{Failure, Success, Try}

class ControllerMock extends ControllerInterface {

  private var _gameState: Option[GameStateInterface] = None
  var strategyPattern: StrategyPattern = StandardRule

  def resetUndoRedo(): Unit = this.resetUndoRedo()

  def setGameState(newState: GameStateInterface): Unit = this.setGameState(newState)

  override def gameState: Try[GameStateInterface] = _gameState match {
    case Some(state) => Success(state)
    case None => Failure(new IllegalStateException("Mock: No GameState"))
  }

  override def updateState(newState: GameStateInterface): Unit = {
    _gameState = Some(newState)
  }

  def initGame(state: GameStateInterface): Unit = {
    _gameState = Some(state)
  }

  override def executeCommand(command: Command): Unit = {
    this.executeCommand(command)
  }

  override def undoCommand(): Unit = {
    this.undoCommand()
  }

  override def redoCommand(): Unit = {
    this.redoCommand()
  }

  override def checkForWinner(): Option[Int] = {
    None
  }

  override def addObserver(observer: Observer): Unit = {
    this.addObserver(observer)
  }

  def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean = {
    true
  }

  def reset(): Unit = {
    _gameState = None
  }

  def canPlaySelected(cards: List[Card], topCard: Card, selectedColor: Option[String]): Boolean = {
    true
  }

  override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
  }

  override val fullDeck: List[Card] = List(
    NumberCard("green", 5),
    ActionCard("red", "skip"),
    WildCard("wild")
  )
}
