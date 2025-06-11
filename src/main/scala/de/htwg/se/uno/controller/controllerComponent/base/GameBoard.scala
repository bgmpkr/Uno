package de.htwg.se.uno.controller.controllerComponent.base

import com.google.inject.{Guice, Inject, Injector}
import de.htwg.se.uno.UnoModule
import de.htwg.se.uno.aview.UnoGame
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.{Command, CommandInvoker, Observable, Observer}

import scala.util.{Failure, Random, Success, Try}

class GameBoard @Inject() extends Observable, ControllerInterface {
  
  private var _gameState: Option[GameStateInterface] = None
  private val invoker = new CommandInvoker()
  val injector: Injector = Guice.createInjector(new UnoModule)

  val fullDeck: List[Card] = createDeckWithAllCards()

  val (discardPile, drawPile) = {
    val shuffled = Random.shuffle(fullDeck)
    (shuffled.headOption.toList, shuffled.tail)
  }

  def gameState: Try[GameStateInterface] = _gameState match {
    case Some(state) => scala.util.Success(state)
    case None => scala.util.Failure(new IllegalStateException("GameState not initialized"))
  }

  private def requireGameState: GameStateInterface =
    gameState.getOrElse(throw new IllegalStateException("GameState is not yet initialized!"))

  def updateState(newState: GameStateInterface): Unit = {
    _gameState = Some(newState)
    notifyObservers()
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

    numberCards.toList ++ actionCards.toList ++ wildCards
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
      hand.isEmpty
    } match {
      case Some((_, winnerIndex)) =>
        Some(winnerIndex)
      case None =>
        None
    }
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

  override def startGame(gameBoard: ControllerInterface, players: Int, cardsPerPlayer: Int): Unit = {
    new Thread(() => {
      val tui = UnoGame.runUno(gameBoard, Some(players), cardsPerPlayer)
    }).start()
  }
}
