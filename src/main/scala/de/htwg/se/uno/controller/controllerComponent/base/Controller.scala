package de.htwg.se.uno.controller.controllerComponent.base

import de.htwg.se.uno.aview.UnoGame
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.fileIOComponent.fileIOJSON.FileIOJson
import de.htwg.se.uno.model.fileIOComponent.fileIOXML.FileIOXml
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.strategy.{StandardRule, StrategyPattern}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.{Command, CommandInvoker, Observable, Observer}

import scala.util.{Failure, Random, Success, Try}

object Controller extends Observable, ControllerInterface {
  private var _gameState: Option[GameStateInterface] = None
  private val invoker = new CommandInvoker()
  var strategyPattern: StrategyPattern = StandardRule
  var fileIO: FileIOInterface = new FileIOJson()
  override val allowDoubleCards: Boolean = false

  val fullDeck: List[Card] = createDeckWithAllCards()

  val (discardPile, drawPile) = {
    val shuffled = Random.shuffle(fullDeck)
    (shuffled.headOption.toList, shuffled.tail)
  }
  var undoStack: List[Command] = Nil
  var redoStack: List[Command] = Nil

  def resetUndoRedo(): Unit = {
    undoStack = Nil
    redoStack = Nil
  }

  def setStrategyPattern(strategy: StrategyPattern): Unit = {
    this.strategyPattern = strategy
  }

  def playTurn(player: PlayerHand, topCard: Card): Unit = {
    val playable = strategyPattern.canPlay(player.cards, topCard)
    println(s"Player can play: $playable")
  }

  def setGameState(newState: GameStateInterface): Unit = {
    _gameState = Some(newState)
    notifyObservers()
  }

  def gameState: Try[GameStateInterface] = _gameState match {
    case Some(state) => Success(state)
    case None => Failure(new IllegalStateException("GameState not initialized"))
  }

  private def requireGameState: GameStateInterface = gameState.get

  def updateState(newState: GameStateInterface): Unit = {
    _gameState = Some(newState)
    notifyObservers()
    fileIO.save(newState)
  }

  def initGame(state: GameStateInterface): Unit = {
    val (discard, draw) = shuffleDeck()
    val initializedState = state.copyWithPiles(draw, discard)
    updateState(initializedState)
  }

  def createDeckWithAllCards(): List[Card] = {
    val numberCards = for {
      color <- Card.colors
      number <- 0 to 9
      count = if (number == 0) 1 else 2
      _ <- 1 to count
    } yield NumberCard(color, number)

    val actionCards = for {
      color <- Card.colors
      action <- Card.actions
      _ <- 1 to 2
    } yield ActionCard(color, action)

    val wildCards = List.fill(4)(WildCard("wild")) ++ List.fill(4)(WildCard("wild draw four"))

    numberCards ++ actionCards ++ wildCards
  }

  def shuffleDeck(): (List[Card], List[Card]) = {
    val shuffled = Random.shuffle(createDeckWithAllCards())
    (shuffled.headOption.toList, shuffled.tail)
  }

  def executeCommand(command: Command): Unit = {
    invoker.executeCommand(command)
  }

  def undoCommand(): Unit = {
    invoker.undoCommand()
  }

  def redoCommand(): Unit = {
    invoker.redoCommand()
  }

  def checkForWinner(): Option[Int] = {
    requireGameState.players.zipWithIndex.find { case (hand, _) =>
      hand.cards.isEmpty && hand.hasSaidUno
    }.map(_._2)
  }

  override def addObserver(observer: Observer): Unit = {
     super.addObserver(observer)
  }

  def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean = {
    requireGameState.isValidPlay(card, Some(topCard), selectedColor)
  }

  def reset(): Unit = {
    _gameState = None
  }

  def canPlaySelected(cards: List[Card], topCard: Card): List[Card] = {
    strategyPattern.canPlay(cards, topCard)
  }

  def setDoubleCardRule(allow: Boolean): Unit = {
    gameState match {
      case scala.util.Success(state: GameState) =>
        val updatedState = state.copy(allowDoubleCards = allow)
        updateState(updatedState)

      case scala.util.Success(_) =>
        println("❌ Cannot update rule - GameState is not valid.")

      case scala.util.Failure(ex) =>
        println("❌ Cannot update rule - GameState not initialized.")
    }
  }

  def input(command: String): Unit = {
    gameState match {
      case scala.util.Success(state) =>
        state.inputHandler(command, Controller) match {
          case de.htwg.se.uno.model.gameComponent.Success(newState: GameStateInterface) => updateState(newState)
          case de.htwg.se.uno.model.gameComponent.Failure(message) =>
            println(s"❌ Input error: $message")
        }

      case scala.util.Failure(_) =>
        println("❌ No valid GameState to handle input.")
    }
  }

  override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
    new Thread(() => {
      val tui = UnoGame.runUno(Some(players), cardsPerPlayer)
    }).start()
  }
}
