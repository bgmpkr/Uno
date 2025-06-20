package de.htwg.se.uno.controller.controllerComponent.base.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.Command

case class ColorWishCommand(color: String) (using gameBoard: ControllerInterface) extends Command {

  private var previousState: Option[GameStateInterface] = None

  override def execute(): Unit = {
    gameBoard.gameState.foreach { state =>
      previousState = Some(state)

      val updatedState: GameStateInterface = state.setSelectedColor(color)

      gameBoard.updateState(updatedState)

      println(s"Chose color for Wild Card: $color")
    }
  }

  override def undo(): Unit = {
    previousState.foreach { oldState =>
      gameBoard.updateState(oldState)
      println(s"Undo: color reseted")
    }
  }

  override def redo(): Unit = execute()
}
