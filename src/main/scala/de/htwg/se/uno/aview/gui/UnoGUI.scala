package de.htwg.se.uno.aview.gui

import de.htwg.se.uno.aview.UnoGame
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.util.Observer
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.Includes.jfxScene2sfx
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.paint.Color.*
import scalafx.scene.paint.*

class UnoGUI(controller: ControllerInterface) extends JFXApp3 with Observer {

  override def update(): Unit = {
    println("Observer update called: Gamestate changed.")
  }

  override def start(): Unit = {
    GameBoard.addObserver(this)

    stage = new PrimaryStage {
      title = "Uno"
      scene = new Scene {
        fill = Color.rgb(40, 40, 40)
        content = StartScreen(() => {
          stage.scene().root = SetupScreen(stage, controller)
        })
      }
    }
  }

  def startGame(players: Int, cards: Int): Unit = {
    new Thread(() => {
      val tui = UnoGame.runUno(Some(players), cards)
    }).start()
  }
}
