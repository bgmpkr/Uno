package de.htwg.se.uno.controller.controllerComponent.base

import com.google.inject.Inject
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.Command

import scala.util.Try

class GameBoardDI @Inject()(fileIO: FileIOInterface) extends ControllerInterface {
  GameBoard.fileIO = fileIO

  def resetUndoRedo(): Unit = GameBoard.resetUndoRedo()
  def initGame(state: GameStateInterface): Unit = GameBoard.initGame(state)

  def gameState: Try[GameStateInterface] = GameBoard.gameState

  override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
    GameBoard.fileIO = fileIO
    GameBoard.startGame(players, cardsPerPlayer)
  }

  override def updateState(newState: GameStateInterface): Unit = {
    GameBoard.fileIO = fileIO
    GameBoard.updateState(newState)
  }

  override def undoCommand(): Unit = GameBoard.undoCommand()

  override def redoCommand(): Unit = GameBoard.redoCommand()

  override def executeCommand(cmd: Command): Unit = GameBoard.executeCommand(cmd)

  override def checkForWinner(): Option[Int] = GameBoard.checkForWinner()

  override val fullDeck: List[Card] = GameBoard.fullDeck

  override def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean =
    GameBoard.isValidPlay(card, topCard, selectedColor)
}
