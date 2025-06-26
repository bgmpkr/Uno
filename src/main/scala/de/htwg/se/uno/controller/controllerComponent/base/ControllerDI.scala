package de.htwg.se.uno.controller.controllerComponent.base

import com.google.inject.Inject
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.strategy.{StandardRule, StrategyPattern}
import de.htwg.se.uno.util.Command

import scala.util.Try

class ControllerDI @Inject()(fileIO: FileIOInterface) extends ControllerInterface {
  Controller.fileIO = fileIO

  def resetUndoRedo(): Unit = Controller.resetUndoRedo()
  def initGame(state: GameStateInterface): Unit = Controller.initGame(state)

  def setGameState(newState: GameStateInterface): Unit = Controller.setGameState(newState)

  def gameState: Try[GameStateInterface] = Controller.gameState

  override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
    Controller.fileIO = fileIO
    Controller.startGame(players, cardsPerPlayer)
  }

  override def updateState(newState: GameStateInterface): Unit = {
    Controller.fileIO = fileIO
    Controller.updateState(newState)
  }

  override def undoCommand(): Unit = Controller.undoCommand()

  override def redoCommand(): Unit = Controller.redoCommand()

  override def executeCommand(cmd: Command): Unit = Controller.executeCommand(cmd)

  override def checkForWinner(): Option[Int] = Controller.checkForWinner()

  override val fullDeck: List[Card] = Controller.fullDeck

  override def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean =
    Controller.isValidPlay(card, topCard, selectedColor)

  def canPlaySelected(cards: List[Card], topCard: Card, selectedColor: Option[String]): Boolean = {
    Controller.canPlaySelected(cards, topCard, selectedColor)
  }
  var strategyPattern: StrategyPattern = Controller.strategyPattern
}
