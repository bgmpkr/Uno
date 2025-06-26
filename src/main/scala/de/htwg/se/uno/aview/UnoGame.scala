package de.htwg.se.uno.aview

import de.htwg.se.uno.controller.controllerComponent.base.Controller
import de.htwg.se.uno.controller.controllerComponent.base.command.UnoCalledCommand

import scala.annotation.tailrec
import scala.io.StdIn.readLine
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand

import scala.util.{Failure, Success, Try}

object UnoGame {
  def runUno(numberPlayers: Option[Int] = None, cardsPerPlayer: Int = 7): UnoTUI = {
    println("Welcome to UNO!")

    val players = numberPlayers.getOrElse(readValidInt("How many players? (2-10): ", min = 2, max = 10))

    val fullDeck = scala.util.Random.shuffle(Controller.createDeckWithAllCards())
    val (newDrawPile, playerHands) = dealCards(fullDeck, players, cardsPerPlayer)

    val (firstCard, remainingDrawPile) = newDrawPile match {
      case head :: tail => (head, tail)
      case Nil => throw new IllegalStateException("No cards left in draw pile")
    }

    val gameState = GameState(
      players = playerHands,
      currentPlayerIndex = 0,
      allCards = fullDeck,
      isReversed = false,
      drawPile = remainingDrawPile,
      discardPile = List(firstCard)
    )

    Controller.initGame(gameState: GameStateInterface)
    println("Let's start the Game!")
    Thread.sleep(2000)

    val initialGameState = Controller.gameState

    Controller.gameState match {
      case scala.util.Success(initialGameState) =>
        val controller = Controller
        val tui = new UnoTUI(controller)
        tui.display()
        inputLoop(tui)
        tui

      case scala.util.Failure(exception: Throwable) =>
        throw new IllegalStateException("Game state not initialized", exception)
    }
  }

  private def dealCards(drawPile: List[Card], playerCount: Int, cardsPerPlayer: Int): (List[Card], List[PlayerHand]) = {
    @tailrec
    def dealHelper(remainingPile: List[Card], players: List[PlayerHand], cardsToDeal: Int): (List[Card], List[PlayerHand]) = {
      if (cardsToDeal == 0) (remainingPile, players)
      else {
        val (newPile, newPlayers) = players.foldLeft((remainingPile, List.empty[PlayerHand])) {
          case ((pile, acc), player) =>
            pile match {
              case card :: tail =>
                (tail, acc :+ PlayerHand(player.cards :+ card))
              case Nil =>
                throw new IllegalStateException("Not enough cards in draw pile")
            }
        }
        dealHelper(newPile, newPlayers, cardsToDeal - 1)
      }
    }

    val initialPlayers = List.fill(playerCount)(PlayerHand(List.empty[Card]))
    dealHelper(drawPile, initialPlayers, cardsPerPlayer)
  }

  @tailrec
  def inputLoop(tui: UnoTUI): Unit = {
    val currentState = Controller.gameState
    Controller.gameState match {
      case scala.util.Success(currentState) =>

        if (currentState.players.exists(_.cards.isEmpty)) {
          println("Game over. GG!")
          System.exit(0)
          return
        }

        val name = currentState.currentPlayerIndex
        val currentPlayer = currentState.players(currentState.currentPlayerIndex)

        if (currentPlayer.cards.length == 1 && !currentPlayer.hasSaidUno) {
          Controller.executeCommand(UnoCalledCommand(Controller))
          println(s"$name said UNO!")
        }

        val input = readLine().trim
        input match {
          case "exit" =>
            println("Game exited.")
            System.exit(0)

          case _ =>
            tui.handleInput(input)

            tui.display()
            inputLoop(tui)
        }

      case scala.util.Failure(exception) =>
        println(s"Error: Game state not initialized: $exception")
        System.exit(1)
    }
  }

  private def readValidInt(prompt: String, min: Int, max: Int): Int = {
    var numberOpt: Option[Int] = None

    while (numberOpt.isEmpty) {
      print(prompt)
      val input = readLine()

      Try(input.toInt) match {
        case Success(parsed) if parsed >= min && parsed <= max =>
          numberOpt = Some(parsed)
        case Success(_) =>
          println(s"Please enter a number between $min and $max.")
        case Failure(_) =>
          println("Invalid input. Please enter a number.")
      }
    }

    numberOpt.get
  }
}