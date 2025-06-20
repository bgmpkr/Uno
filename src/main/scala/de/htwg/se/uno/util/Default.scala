package de.htwg.se.uno.util

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard

object Default {
  given ControllerInterface = GameBoard
}
