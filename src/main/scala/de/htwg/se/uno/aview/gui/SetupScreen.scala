package de.htwg.se.uno.aview.gui

import com.google.inject.Guice
import de.htwg.se.uno.UnoModule
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Button, ComboBox}
import scalafx.geometry.Pos
import scalafx.geometry.Insets
import scalafx.scene.{Cursor, Scene}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color.{DarkRed, LightGrey, White}
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stops}
import scalafx.scene.text.Text

object SetupScreen {

  def apply(primaryStage: PrimaryStage, gameBoard: ControllerInterface): StackPane = {
    val injector = Guice.createInjector(new UnoModule)
    val screenFactory = injector.getInstance(classOf[ScreenFactory])

    val setupImage = new ImageView(new Image("file:src/main/resources/UnoSetup.jpg")) {
      fitWidth = 1400
      fitHeight = 900
      preserveRatio = false
    }

    val defaultCardsPerPlayer = 7

    val playersInput = new ComboBox[Int](ObservableBuffer(2 to 10: _*)) {
      promptText = "Number of Players (2-10)"
      margin = Insets(20)
      minWidth = 300
      minHeight = 40
      style = """
        -fx-background-color: #F2F2F2;
        -fx-text-fill: #8B0000;
        -fx-font-size: 14pt;
        -fx-font-weight: bold;
        -fx-prompt-text-fill: derive(#8B0000, 30%);
        -fx-border-color: #8B0000;
        -fx-border-width: 2;
        -fx-border-radius: 5;
        -fx-background-radius: 5;
      """

      cursor = Cursor.Hand
    }

    val startButton = new Button("Start Game") {
      style = "-fx-font-family: 'sans-serif'; " +
        "-fx-font-style: italic; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 20pt; " +
        "-fx-background-color: linear-gradient(to bottom, #FF4500, #8B0000); " +
        "-fx-text-fill: white; " +
        "-fx-padding: 10 20; " +
        "-fx-background-radius: 10; " +
        "-fx-border-radius: 10;"
      effect = new DropShadow {
        color = Color.DarkRed
        radius = 10
      }

      cursor = Cursor.Hand

      onAction = _ => {
        try {
          playersInput.value.value match {
            case players if players >= 2 && players <= 10 =>
              gameBoard.startGame(players, defaultCardsPerPlayer)
              val gameScreen = new GameScreen(players, defaultCardsPerPlayer, gameBoard, screenFactory)
              primaryStage.scene = new Scene(gameScreen) {
                fill = Color.DarkRed
              }

            case _ =>
              println("Please select a valid number of players (2-10)!")
          }
        } catch {
          case _: NullPointerException =>
            println("Please select a number of players first!")
          case e: Exception =>
            println(s"Unexpected error: ${e.getMessage}")
        }
      }
    }

    val layout = new VBox {
      spacing = 20
      alignment = Pos.TopCenter
      children = Seq(
        new Text("Setup") {
          style = "-fx-font: italic bold 100pt sans-serif"
          fill = new LinearGradient(
            endX = 0,
            stops = Stops(White, Color.LightGrey)
          )
          margin = Insets(30,0,0,0)
          effect = new DropShadow {
            color = Color.DarkRed
            radius = 15
            spread = 0.25
          }
        },
        playersInput,
        startButton
      )
    }

    new StackPane {
      children = Seq(
        setupImage,
        new StackPane {
          alignment = Pos.TopCenter
          padding = Insets(150,0,0,0)
          children = layout
        }
      )
    }
  }
}
