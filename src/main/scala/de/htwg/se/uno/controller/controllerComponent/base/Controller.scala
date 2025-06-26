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
import de.htwg.se.uno.util.{Command, CommandInvoker, Observable, Observer}

import scala.util.{Failure, Random, Success, Try}

object Controller extends Observable, ControllerInterface {
  private var _gameState: Option[GameStateInterface] = None
  private val invoker = new CommandInvoker()
  var fileIO: FileIOInterface = new FileIOJson()

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

    numberCards.toList ++ actionCards.toList ++ wildCards
  }

  def shuffleDeck(): (List[Card], List[Card]) = {
    val shuffled = Random.shuffle(createDeckWithAllCards())
    (shuffled.headOption.toList, shuffled.tail)
  }

  def executeCommand(command: Command): Unit = {
    invoker.executeCommand(command)
    // _gameState.foreach(s => JsonFileIO.save(s.asInstanceOf[GameState]))
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

  override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
    new Thread(() => {
      val tui = UnoGame.runUno(Some(players), cardsPerPlayer)
    }).start()
  }
}
