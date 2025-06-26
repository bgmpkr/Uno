package de.htwg.se.uno.controller.controllerComponent.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.Controller
import de.htwg.se.uno.controller.controllerComponent.base.command.ColorWishCommand
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.Command
import org.scalatest.BeforeAndAfterEach

class ColorWishCommandSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    Controller.updateState(initialState)
    Controller.resetUndoRedo()
  }
  
  val initialState = GameState(
    players = List(),
    currentPlayerIndex = 0,
    allCards = List(),
    isReversed = false,
    discardPile = List(),
    drawPile = List(),
    selectedColor = None
  )
  val dummyController = new ControllerInterface {
    private var currentGameState: GameStateInterface = initialState
    val red5: NumberCard = NumberCard("red", 5)
    val blue5: NumberCard = NumberCard("blue", 5)
    val redDraw2: ActionCard = ActionCard("red", "draw two")
    val wildCard: WildCard = WildCard("wild")
    val drawFour: WildCard = WildCard("wild draw four")
    val allCards: List[Card] = List(red5, blue5, redDraw2, wildCard, drawFour)
    override val fullDeck: List[Card] = allCards
    var undoStack: List[Command] = Nil
    var redoStack: List[Command] = Nil

    override def resetUndoRedo(): Unit = {
      undoStack = Nil
      redoStack = Nil
    }
    override def gameState: scala.util.Try[GameStateInterface] = scala.util.Success(currentGameState)

    override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
      currentGameState = new GameState(
        players = List.fill(players)(PlayerHand(List.fill(cardsPerPlayer)(allCards.head))),
        currentPlayerIndex = 0,
        allCards = allCards,
        isReversed = false,
        discardPile = List(allCards.last),
        drawPile = allCards.drop(cardsPerPlayer * players + 1),
        selectedColor = None,
        currentPhase = None
      )
      notifyObservers()
    }

    override def updateState(newState: GameStateInterface): Unit = {
      currentGameState = newState
      notifyObservers()
    }

    override def initGame(state: GameStateInterface): Unit = updateState(state)

    override def undoCommand(): Unit = {
      notifyObservers()
    }

    override def redoCommand(): Unit = {
      notifyObservers()
    }

    override def executeCommand(cmd: Command): Unit = {
      cmd.execute()
      notifyObservers()
    }

    override def checkForWinner(): Option[Int] = {
      currentGameState.players.indexWhere(_.cards.isEmpty) match {
        case -1 => None
        case index => Some(index)
      }
    }

    override def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean = {
      card.color == topCard.color ||
        selectedColor.contains(card.color)
    }
  }

  "ColorWishCommand" should {

    "set the selected color in GameBoard on execute" in {
      val initialState = GameState(
        players = List(),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List(),
        selectedColor = Some("red")
      )

      Controller.updateState(initialState)

      val command = ColorWishCommand("red", dummyController)
      command.execute()

      dummyController.gameState.get.selectedColor shouldBe Some("red")
      command.execute()

      Controller.gameState.get.selectedColor shouldBe Some("red")
    }

    "restore the previous state on undo" in {
      val initialState = GameState(
        players = List(),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List(),
        selectedColor = Some("blue")
      )

      Controller.updateState(initialState)

      val command = ColorWishCommand("yellow", dummyController)
      command.execute()

      Controller.gameState.get.selectedColor shouldBe Some("blue")

      command.undo()
      Controller.gameState.get.selectedColor shouldBe Some("blue")
    }

    "reapply the color change on redo" in {
      val initialState = GameState(
        players = List(),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List(),
        selectedColor = Some("blue")
      )
      
      Controller.updateState(initialState)

      val command = ColorWishCommand("blue", dummyController)
      command.execute()
      command.undo()
      command.redo()

      Controller.gameState.get.selectedColor shouldBe Some("blue")
    }
  }
}
