package de.htwg.se.uno.aview.gui

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.command.{DrawCardCommand, PlayCardCommand, UnoCalledCommand}
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, CardFactory, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand
import scalafx.animation.{FadeTransition, PauseTransition}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Alert, Button, ButtonBar, ButtonType, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.Cursor
import scalafx.util.Duration
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.scene.effect.DropShadow

import scala.util.{Failure, Success}

class GameScreen(players: Int, cardsPerPlayer: Int) (using gameBoard: ControllerInterface) extends StackPane {
  private var unoCaller: Option[Int] = None
  private var gameOver: Boolean = false

  val allCards: List[Card] = CardFactory.createFullDeck()
  private val hands = allCards.grouped(cardsPerPlayer).take(players).map(cards => PlayerHand(cards)).toList
  private val usedCards = hands.flatMap(_.cards)
  private val remainingDeck = allCards.diff(usedCards)
  private val initialDiscard = remainingDeck.head
  private val updatedRemainingDeck = remainingDeck.tail

  gameBoard.initGame(GameState(
    players = hands,
    currentPlayerIndex = 0,
    isReversed = false,
    drawPile = updatedRemainingDeck,
    discardPile = List(initialDiscard),
    allCards = allCards
  ))

  private val gameBoardImage = new ImageView(new Image("file:src/main/resources/gameboard/uno_gameboard_left.jpg")) {
    fitWidth = 1500
    fitHeight = 880
    preserveRatio = true
    smooth = true
    cache = true
    mouseTransparent = true
  }

  private val winnerLabel = new Label {
    text = ""
    font = Font.font("Arial", FontWeight.Bold, 48)
    textFill = Color.Gold
    style = "-fx-background-color: rgba(0,0,0,0.8); -fx-background-radius: 20; -fx-padding: 30;"
    visible = false
  }

  private val gameInfo = new Label {
    text = s"Players: $players | Cards: $cardsPerPlayer"
    font = Font(24)
    textFill = Color.White
    style = "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10; -fx-background-radius: 10;"
  }

  private val player1HandView = new HBox {
    spacing = -60
    alignment = Pos.BottomCenter
    padding = Insets(10)
    pickOnBounds = false
  }

  private val player2HandView = new HBox {
    spacing = -60
    alignment = Pos.TopCenter
    padding = Insets(10)
    pickOnBounds = false
  }

  private val discardPileView = new VBox {
    spacing = 5
    alignment = Pos.Center
    pickOnBounds = false
  }

  private val drawPileView = new ImageView {
    image = new Image("file:src/main/resources/cards/back.png")
    fitWidth = 130
    fitHeight = 190
    preserveRatio = true
    cursor = Cursor.Hand
    pickOnBounds = true
    onMouseClicked = (e: MouseEvent) => {
      println("Draw pile clicked!")
      gameBoard.executeCommand(DrawCardCommand()(using gameBoard))
      update()
      e.consume()
    }
  }

  private val drawButton = new Button("Draw") {
    style = buttonStyle
    onAction = _ => {
      gameBoard.executeCommand(DrawCardCommand()(using gameBoard))
      update()
    }
  }

  private val unoButton = new Button("UNO!") {
    style = buttonStyle
    onAction = _ => {
      gameBoard.executeCommand(UnoCalledCommand()(using gameBoard))
      unoCaller = gameBoard.gameState.toOption.map(_.currentPlayerIndex)
      update()
    }
  }

  private val exitButton = new Button("Exit") {
    style = buttonStyle
    onAction = _ => System.exit(0)
  }

  private val youLabel = new Label {
    text = "You"
    style = "-fx-text-fill: white; -fx-font-size: 20pt;"
  }

  private val playerLeftHandView = new VBox {
    spacing = -60
    alignment = Pos.CenterLeft
    padding = Insets(10)
    pickOnBounds = false
  }

  private val playerRightHandView = new VBox {
    spacing = -60
    alignment = Pos.CenterRight
    padding = Insets(10)
    pickOnBounds = false
  }

  private def updateBackground(isReversed: Boolean): Unit = {
    val imagePath = if (isReversed)
      "file:src/main/resources/gameboard/uno_gameboard_left.jpg"
    else
      "file:src/main/resources/gameboard/uno_gameboard_right.jpg"
    gameBoardImage.image = new Image(imagePath)
  }


  children = Seq(
    gameBoardImage,

    new VBox {
      alignment = Pos.TopLeft
      padding = Insets(20)
      children = Seq(exitButton)
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.TopCenter
      spacing = 10
      children = Seq(player2HandView)
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.BottomCenter
      spacing = 10
      children = Seq(
        new Label {
          text = s"You (Player ${gameBoard.gameState.map(_.currentPlayerIndex + 1).getOrElse(1)})"
          style = "-fx-text-fill: white; -fx-font-size: 20pt;"
        },
        player1HandView
      )
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.CenterLeft
      spacing = 10
      children = Seq(playerLeftHandView)
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.CenterRight
      spacing = 10
      children = Seq(playerRightHandView)
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.Center
      spacing = 20
      children = Seq(
        new StackPane {
          children = drawPileView
          pickOnBounds = false
        },
        discardPileView
      )
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.BottomRight
      padding = Insets(0, 150, 100, 0)
      spacing = 10
      children = Seq(drawButton, unoButton)
      pickOnBounds = false
    },

    new VBox {
      alignment = Pos.TopRight
      padding = Insets(20)
      children = Seq(gameInfo)
      pickOnBounds = false
    },

    new StackPane {
      alignment = Pos.Center
      children = Seq(winnerLabel)
      pickOnBounds = false
    }
  )


  private def buttonStyle: String =
    "-fx-font-family: 'sans-serif'; " +
      "-fx-font-style: italic; " +
      "-fx-font-weight: bold; " +
      "-fx-font-size: 15pt; " +
      "-fx-background-color: linear-gradient(to bottom, #FCE205, #F9A602); " +
      "-fx-text-fill: white; " +
      "-fx-padding: 10 20; " +
      "-fx-background-radius: 10; " +
      "-fx-border-radius: 10;"

  private def createCardView(cards: List[Card], hidden: Boolean = false): Seq[ImageView] = {
    cards.zipWithIndex.map { case (card, index) =>
      val imagePath = cardImagePath(card)
      val cardView = new ImageView(new Image(imagePath)) {
        fitWidth = 130
        fitHeight = 190
        preserveRatio = true
        pickOnBounds = true

        if (!hidden && !gameOver) {
          cursor = Cursor.Hand
          onMouseClicked = (e: MouseEvent) => {
            println(s"Card $index clicked!")
            e.consume()
            playCard(card)
          }

          onMouseEntered = _ => {
            scaleX = 1.2
            scaleY = 1.2
            translateY = -20
            effect = new DropShadow {
              radius = 20
              color = Color.Black
            }
          }

          onMouseExited = _ => {
            scaleX = 1.0
            scaleY = 1.0
            translateY = 0
            effect = null
          }

          style = "-fx-effect: dropshadow(gaussian, white, 5, 0.5, 0, 0);"
        }
      }

      cardView
    }
  }


  private def cardImagePath(card: Card): String = card match {
    case NumberCard(color, number) => s"file:src/main/resources/cards/${number}_$color.png"
    case ActionCard(color, action) => action match {
      case "reverse" => s"file:src/main/resources/cards/reverse_$color.png"
      case "skip" => s"file:src/main/resources/cards/next_$color.png"
      case "draw two" => s"file:src/main/resources/cards/draw2_$color.png"
      case _ => "file:src/main/resources/cards/unknown.png"
    }
    case WildCard(action) => action match {
      case "wild draw four" => "file:src/main/resources/cards/draw_four.png"
      case "wild" => "file:src/main/resources/cards/wild.png"
      case _ => "file:src/main/resources/cards/unknown.png"
    }
  }

  def playCard(card: Card): Unit = {
    gameBoard.gameState match {
      case Success(state) =>
        val oldTop = state.discardPile.headOption.getOrElse(card)

        card match {
          case wc: WildCard =>
            showColorPickerDialog(wc)
          case _ =>
            gameBoard.executeCommand(PlayCardCommand(card, None) (using gameBoard))
            update()

            val pause = new PauseTransition(Duration(400))
            pause.setOnFinished(_ => {
              val afterPlayState = gameBoard.gameState
              afterPlayState match {
                case Success(newState) =>
                  val isValid = gameBoard.isValidPlay(card, oldTop, None)
                  if (!isValid) {
                    Platform.runLater {
                      gameBoard.undoCommand()
                      gameBoard.executeCommand(DrawCardCommand()(using gameBoard))
                      update()
                      showInvalidMoveMessage()
                    }
                  } else {
                    update()
                  }

                case Failure(e) => println(s"State error: ${e.getMessage}")
              }
            })

            pause.play()
        }

      case Failure(e) => println(s"Initial game state error: ${e.getMessage}")
    }
  }


  private def showInvalidMoveMessage(): Unit = {
    new Alert(Alert.AlertType.Warning) {
      title = "Nice try..."
      headerText = "This card can not be played"
      contentText = "You must draw a penalty card ¯\\_(ツ)_/¯."
    }.showAndWait()
  }


  private def showColorPickerDialog(wildCard: WildCard): Unit = {
    val alert = new Alert(Alert.AlertType.Confirmation) {
      title = "Choose Color"
      headerText = "Choose a color for the wildcard"
      contentText = "Which color do you want to choose?"
    }

    val redButton = new ButtonType("Red")
    val blueButton = new ButtonType("Blue")
    val greenButton = new ButtonType("Green")
    val yellowButton = new ButtonType("Yellow")
    val cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CancelClose)

    alert.buttonTypes = Seq(redButton, blueButton, greenButton, yellowButton, cancelButton)

    alert.showAndWait() match {
      case Some(`redButton`) => playWildCard(wildCard, "red")
      case Some(`blueButton`) => playWildCard(wildCard, "blue")
      case Some(`greenButton`) => playWildCard(wildCard, "green")
      case Some(`yellowButton`) => playWildCard(wildCard, "yellow")
      case _ => println("Color selection cancelled")
    }
  }

  private def playWildCard(wildCard: WildCard, color: String): Unit = {
    println(s"Playing wildcard with color: $color")

    gameBoard.executeCommand(PlayCardCommand(wildCard, Some(color)) (using gameBoard))
    gameBoard.gameState match {
      case Success(newState) =>
        println(s"Wildcard played successfully, new player index: ${newState.currentPlayerIndex}")
        update()
      case Failure(e) =>
        println(s"Error after playing wildcard: ${e.getMessage}")
        showInvalidMoveMessage()
    }
  }

  def update(): Unit = {
    if (gameOver) return

    gameBoard.gameState match {
        case Success(state) =>
          updateBackground(state.isReversed)
          state.players.zipWithIndex.find(_._1.cards.isEmpty) match {
            case Some((_, index)) =>
              gameOver = true
              winnerLabel.text = s"Player ${index + 1} has won!"
              winnerLabel.visible = true
              drawButton.disable = true
              unoButton.disable = true
              player1HandView.children.foreach(_.setDisable(true))
              player2HandView.children.foreach(_.setDisable(true))
              return
            case None =>
          }

          if (unoCaller.isDefined) {
            val unoPlayer = unoCaller.get + 1
            gameInfo.text = s"Player $unoPlayer calls UNO!"
            unoCaller = None
          } else {
            gameInfo.text = s"It is Player ${state.currentPlayerIndex + 1}'s turn"
          }

          youLabel.text = s"You (Player ${state.currentPlayerIndex + 1})"

          val pause = new PauseTransition(Duration(2000))
          val fade = new FadeTransition(Duration(1000), gameInfo) {
            fromValue = 1.0
            toValue = 0.0
          }
          pause.onFinished = _ => fade.play()
          pause.play()

          discardPileView.children.setAll(createCardView(state.discardPile.take(1)).map(_.delegate): _*)

          val currentPlayerHand = state.players(state.currentPlayerIndex).cards
          player1HandView.children.setAll(createCardView(currentPlayerHand).map(_.delegate): _*)

          val backImage = new Image("file:src/main/resources/cards/back.png")
          val playerCount = state.players.length
          val currentPlayerIndex = state.currentPlayerIndex

          playerCount match {
            case 2 =>
              //cards of other player
              val nextPlayerIndex = (currentPlayerIndex + 1) % playerCount
              val nextPlayerCardsCount = state.players(nextPlayerIndex).cards.length
              val backCards = Seq.fill(nextPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                  rotate = 180
                }
              }
              //cards top
              player2HandView.children.setAll(backCards.map(_.delegate): _*)

              //cards left/ right clean
              playerLeftHandView.children.clear()
              playerRightHandView.children.clear()

            case 3 =>
              //cards left player
              val leftPlayerIndex = (currentPlayerIndex + 1) % playerCount
              val leftPlayerCardsCount = state.players(leftPlayerIndex).cards.length
              val leftBackCards = Seq.fill(leftPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                  rotate = 90
                }
              }
              playerLeftHandView.children.setAll(leftBackCards.map(_.delegate): _*)

              //cards top player
              val topPlayerIndex = (currentPlayerIndex + 2) % playerCount
              val topPlayerCardsCount = state.players(topPlayerIndex).cards.length
              val topBackCards = Seq.fill(topPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                }
              }
              player2HandView.children.setAll(topBackCards.map(_.delegate): _*)
              playerRightHandView.children.clear()

            case x if x >= 4 =>
              //cards left player
              val leftPlayerIndex = (currentPlayerIndex + 1) % playerCount
              val leftPlayerCardsCount = state.players(leftPlayerIndex).cards.length
              val leftBackCards = Seq.fill(leftPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                  rotate = 90
                }
              }

              //cards player right
              val rightPlayerIndex = (currentPlayerIndex + 2) % playerCount
              val rightPlayerCardsCount = state.players(rightPlayerIndex).cards.length
              val rightBackCards = Seq.fill(rightPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                  rotate = -90
                }
              }

              //cards top player
              val topPlayerIndex = (currentPlayerIndex + 3) % playerCount
              val topPlayerCardsCount = state.players(topPlayerIndex).cards.length
              val topBackCards = Seq.fill(topPlayerCardsCount) {
                new ImageView(backImage) {
                  fitWidth = 130
                  fitHeight = 190
                  preserveRatio = true
                  rotate = 180
                }
              }

              playerLeftHandView.children.setAll(leftBackCards.map(_.delegate): _*)
              playerRightHandView.children.setAll(rightBackCards.map(_.delegate): _*)
              player2HandView.children.setAll(topBackCards.map(_.delegate): _*)

            case _ =>
              playerLeftHandView.children.clear()
              playerRightHandView.children.clear()
              player2HandView.children.clear()
          }

        case Failure(e) =>
          gameInfo.text = s"Error: ${e.getMessage}"
      }
  }
  update()
  gameBoard.addObserver(() => update())
}
