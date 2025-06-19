package de.htwg.se.uno.model.fileIOComponent.fileIOJSON

import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import play.api.libs.json.*

import java.io.{File, PrintWriter}
import scala.io.Source
import de.htwg.se.uno.model.JsonFormats.*
import de.htwg.se.uno.model.gameComponent.GameStateInterface

class FileIOJson extends FileIOInterface {
  val savedir = "src/main/scala/de/htwg/se/uno/model/fileIOComponent/data/"
  override def save(gameState: GameStateInterface, file: String = "Uno.json"): Unit = {
    new File(savedir).mkdirs()
    val concreteState = gameState.asInstanceOf[GameState]
    val jsonFile = new File(savedir + file)
    val pw = new PrintWriter(jsonFile)
    pw.write(Json.prettyPrint(Json.toJson(concreteState)))
    pw.close()
  }

  override def load(file: String = "Uno.json"): GameStateInterface = {
    val jsonFile = new File(savedir + file)
    val source = scala.io.Source.fromFile(jsonFile)
    val content = source.mkString
    source.close()
    Json.parse(content).as[GameState]
  }
}
