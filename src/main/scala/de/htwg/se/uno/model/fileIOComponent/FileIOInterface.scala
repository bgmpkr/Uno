package de.htwg.se.uno.model.fileIOComponent

import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import play.api.libs.json.Json
import de.htwg.se.uno.model.JsonFormats.*
import java.io.{File, PrintWriter}

trait FileIOInterface {
  
  def save(gameState: GameStateInterface, file: String = ""): Unit
  def load(file: String = ""): GameStateInterface
}
