package de.htwg.se.uno.aview

import de.htwg.se.uno.aview.ColorPrinter.*
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.controller.controllerComponent.base.command.{DrawCardCommand, PlayCardCommand, UnoCalledCommand}
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.WildCard
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.Observer

import scala.io.StdIn.readLine

class UnoTUI(controller: ControllerInterface) extends Observer {

  private var gameShouldExit = false
  var selectedColor: Option[String] = None

  GameBoard.addObserver(this)

  def display(): Unit = {
    val state = GameBoard.gameState
    GameBoard.gameState match {
      case scala.util.Success(state) =>
        if (state.players.isEmpty || gameShouldExit) return

        val currentPlayer = state.players(state.currentPlayerIndex)
        val topCard = state.discardPile.headOption.getOrElse(return)

        println("\n--------------------------------------------------------------------")
        println(s"Player ${state.currentPlayerIndex + 1}'s turn!")

        val unoPlayers = state.players.zipWithIndex.filter(_._1.hasSaidUno)
        unoPlayers.foreach { case (_, idx) =>
          if (idx == state.currentPlayerIndex) println("You said 'UNO'!")
          else println(s"Player ${idx + 1} said UNO")
        }

        print("Top Card: ")
        printCard(topCard)
        selectedColor.foreach(c => println(s"The color that was chosen: $c"))

        showHand(currentPlayer)

        if (!currentPlayer.cards.exists(card => state.isValidPlay(card, Some(topCard), selectedColor))) {
          println("No playable Card! You have to draw a card...")
          GameBoard.executeCommand(DrawCardCommand(controller))
          gameShouldExit = false
          display()
        } else {
          println("Select a card (index) to play or type 'draw' to draw a card:")
        }

      case scala.util.Failure(exception: Throwable) =>
        println(s"Game state not initialized: $exception")
    }
  }

  def handleInput(input: String): Unit = {
    GameBoard.gameState match {
      case scala.util.Success(state) =>
        val currentPlayer = state.players(state.currentPlayerIndex)

        input match {
          case "draw" =>
            state.drawCardAndReturnDrawn() match {
              case (newState, Some(drawnCard)) =>
                println(s"You drew: $drawnCard")

                if (newState.isValidPlay(drawnCard, newState.discardPile.headOption, newState.selectedColor)) {
                  println("Playing drawn card...")
                  GameBoard.updateState(newState)

                  val chosenColor = None
                    if (drawnCard.isInstanceOf[WildCard]) Some(chooseWildColor())
                    else None

                  GameBoard.executeCommand(PlayCardCommand(drawnCard, chosenColor, GameBoard))
                } else {
                  println("Card cannot be played, turn ends.")
                  val skipped = newState.nextPlayer()
                  GameBoard.updateState(skipped)
                  skipped.notifyObservers()
                }

              case (_, None) =>
                println("❌ No card could be drawn.")
            }

          case "undo" =>
            GameBoard.undoCommand()

          case "redo" =>
            GameBoard.redoCommand()

          case _ =>
            scala.util.Try(input.toInt) match {
              case scala.util.Success(index) if index >= 0 && index < currentPlayer.cards.length =>
                val chosenCard = currentPlayer.cards(index)
                val chosenColor = None
                  if (chosenCard.isInstanceOf[WildCard]) Some(chooseWildColor())
                  else None

                GameBoard.executeCommand(PlayCardCommand(chosenCard, chosenColor, GameBoard))

              case scala.util.Success(_) =>
                println(s"Invalid index.")

              case scala.util.Failure(_) =>
                println("Invalid input. Use card index or 'draw'.")
            }
        }

      checkUno()
      checkForWinner()

      case scala.util.Failure(exception: Throwable) =>
        println(s"Game state not initialized: $exception")
    }
  }

  def chooseWildColor(inputFunc: () => String = () => readLine()): String = {
    val colors = List("red", "green", "blue", "yellow")
    var validColor = false
    var chosenColor = ""

    while (!validColor) {
      println("Please choose a color for the Wild Card:")
      colors.zipWithIndex.foreach { case (c, i) => println(s"$i - $c") }

      inputFunc().trim match {
        case input if input.matches("[0-3]") =>
          chosenColor = colors(input.toInt)
          println(s"Wild Card color changed to: $chosenColor")
          validColor = true
        case _ => println("Invalid input. Please enter a number between 0 and 3.")
      }
    }

    chosenColor
  }

  private def showHand(player: PlayerHand): Unit = {
    println("Your cards:")
    player.cards.zipWithIndex.foreach { case (card, i) =>
      print(s"$i - ")
      printCard(card)
    }
  }

  def checkForWinner(): Unit = {
    GameBoard.checkForWinner() match {
      case Some(idx) =>
        println(s"Player ${idx + 1} wins! Game over.")
        gameShouldExit = true
      case None => ()
    }
  }

  def shouldExit: Boolean = gameShouldExit

  def setShouldExit(value: Boolean): Unit = {
    gameShouldExit = value
  }

  private def checkUno(): Unit = {
    val state = GameBoard.gameState
    GameBoard.gameState match {
      case scala.util.Success(state) =>
        val updatedPlayer = state.players(state.currentPlayerIndex)

        if (updatedPlayer.cards.length == 1 && !updatedPlayer.hasSaidUno) {
          GameBoard.executeCommand(UnoCalledCommand(controller))
          println("You said 'UNO'!")
        }
      case scala.util.Failure(exception: Throwable) =>
        println(s"Game state not initialized: ${exception.getMessage}")
    }

  }

  override def update(): Unit = if (!gameShouldExit) display()
}
