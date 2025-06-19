package de.htwg.se.uno.model.fileIOComponent.fileIOJSON

import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import play.api.libs.json.*

import java.io.{File, PrintWriter}
import scala.io.Source
import de.htwg.se.uno.model.JsonFormats.*
import de.htwg.se.uno.model.gameComponent.GameStateInterface

class FileIOJson extends FileIOInterface {

  override def save(gameState: GameStateInterface): Unit = {
    val concreteState = gameState.asInstanceOf[GameState]
    val pw = new PrintWriter(new File("Uno.json"))
    pw.write(Json.prettyPrint(Json.toJson(concreteState)))
    pw.close()
  }

  override def load(): GameStateInterface = {
    val source = scala.io.Source.fromFile("Uno.json")
    val content = source.mkString
    source.close()
    Json.parse(content).as[GameState]
  }
}
