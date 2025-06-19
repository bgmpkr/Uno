package de.htwg.se.uno

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import net.codingwell.scalaguice.ScalaModule
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.controller.controllerComponent.base.{GameBoard, GameBoardDI}
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.fileIOComponent.fileIOJSON.FileIOJson

class UnoModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bindConstant().annotatedWith(Names.named("DefaultPlayers")).to(2)
    bindConstant().annotatedWith(Names.named("CardsPerPlayer")).to(7)

    bind[GameStateInterface].to[GameState]
    bind[ControllerInterface].to[GameBoardDI]
    bind[FileIOInterface].to[FileIOJson]
  }
}
