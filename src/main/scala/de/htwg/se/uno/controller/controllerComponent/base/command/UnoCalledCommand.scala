package de.htwg.se.uno.controller.controllerComponent.base.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.Command

case class UnoCalledCommand(dummy: Unit = ()) (using gameBoard: ControllerInterface) extends Command {
  private var previousState: Option[GameStateInterface] = None

  override def execute(): Unit = {
    gameBoard.gameState.foreach { state =>
      previousState = Some(state)
      val idx = state.currentPlayerIndex
      val updatedGame = state.playerSaysUno(idx)
      if (updatedGame.players(updatedGame.currentPlayerIndex).cards.isEmpty) {
        gameBoard.updateState(updatedGame.setGameOver())
      } else {
        gameBoard.updateState(updatedGame)
      }
    }
  }

  override def undo(): Unit = {
    previousState.foreach { oldState =>
      gameBoard.updateState(oldState)
    }
  }

  override def redo(): Unit = execute()
}
