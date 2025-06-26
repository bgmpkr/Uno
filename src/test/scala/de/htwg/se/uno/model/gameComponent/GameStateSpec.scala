package de.htwg.se.uno.model.gameComponent.base

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.model.gameComponent.{Failure, GameStateInterface, Success}
import de.htwg.se.uno.util.Command
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

import scala.util.Try

class GameStateSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    GameBoard.updateState(initialState)
    GameBoard.resetUndoRedo()
  }

  val red5: NumberCard = NumberCard("red", 5)
  val blue5: NumberCard = NumberCard("blue", 5)
  val redDraw2: ActionCard = ActionCard("red", "draw two")
  val wildCard: WildCard = WildCard("wild")
  val drawFour: WildCard = WildCard("wild draw four")
  val yellow7: NumberCard = NumberCard("yellow", 7)
  val green9: NumberCard = NumberCard("green", 9)
  val green6: NumberCard = NumberCard("green", 6)

  val allCards: List[Card] = List(red5, blue5, redDraw2, wildCard, drawFour)
  val player1: PlayerHand = PlayerHand(List(redDraw2))
  val player2: PlayerHand = PlayerHand(List(blue5, yellow7))
  val initialPlayers: List[PlayerHand] = List(player1, player2)

  val initialState: GameState = GameState(
    players = initialPlayers,
    currentPlayerIndex = 0,
    allCards = allCards,
    discardPile = List(green9),
    drawPile = List(green9, yellow7),
    isReversed = false
  )
  val dummyController = new ControllerInterface {
    private var currentGameState: GameStateInterface = initialState
    override val fullDeck: List[Card] = allCards
    var undoStack: List[Command] = Nil
    var redoStack: List[Command] = Nil

    override def resetUndoRedo(): Unit = {
      undoStack = Nil
      redoStack = Nil
    }

    override def gameState: Try[GameStateInterface] = scala.util.Success(currentGameState)

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

  "GameState" should {

    "switch to the next player" in {
      val nextState = initialState.nextPlayer()
      nextState.currentPlayerIndex shouldBe 1
    }

    "deal initial cards to all players" in {
      val emptyPlayers = List.fill(2)(PlayerHand(Nil))
      val state = initialState.copy(
        players = emptyPlayers,
        drawPile = List.fill(10)(red5)
      )

      val updatedState = state.dealInitialCards(2)

      updatedState.players.foreach(_.cards.length shouldBe 2)
      updatedState.drawPile.length shouldBe 6
    }


    "identify a winner if a player has no cards" in {
      val winningState = initialState.copy(players = List(PlayerHand(Nil), player2))
      winningState.checkForWinner() shouldBe Some(0)
    }

    "draw a card and return updated state" in {
      val (newState, Some(drawnCard)) = initialState.drawCardAndReturnDrawn()
      newState.players.head.cards should contain(drawnCard)
      newState.drawPile.size shouldBe initialState.drawPile.size - 1
    }

    "play a valid card and update discard pile" in {
      val updatedState = initialState.copy(players = List(PlayerHand(List(green6)), player2))
        .playCard(green6)

      updatedState.discardPile.head shouldBe green6
      updatedState.players.head.cards shouldBe empty
    }

    "validate legal and illegal plays correctly" in {
      val topCard = Some(redDraw2)
      initialState.isValidPlay(redDraw2, topCard) shouldBe true
      initialState.isValidPlay(blue5, topCard) shouldBe false
      initialState.isValidPlay(wildCard, topCard) shouldBe true
    }

    "handle inputHandler for valid wild card play" in {
      val state = initialState.copy(
        players = List(PlayerHand(List(wildCard)), player2)
      )
      val result = state.inputHandler("play wild:0:green", dummyController)
      result shouldBe a[Success]
    }

    "reject invalid input in inputHandler" in {
      val result = initialState.inputHandler("play wild:abc:green", dummyController)
      result shouldBe a[Failure]
    }

    "reject out-of-bounds card index in inputHandler" in {
      val result = initialState.inputHandler("play wild:9:red", dummyController)
      result shouldBe a[Failure]
    }

    "successfully play a valid card using inputHandler" in {
      val player1 = PlayerHand(List(redDraw2, blue5))
      val player2 = PlayerHand(List(blue5, yellow7))
      val players = List(player1, player2)

      val adjustedState = initialState.copy(
        players = players,
        discardPile = List(red5)
      )

      GameBoard.updateState(adjustedState)
      dummyController.updateState(adjustedState)

      val result = adjustedState.inputHandler("play card:0", dummyController)

      result match {
        case Success(newState) =>
          newState.players.head.cards should not contain redDraw2
          newState.discardPile.head shouldBe redDraw2

        case _ =>
          fail("Expected Success but got Failure or other")
      }
    }


    "fail inputHandler with invalid card index" in {
      GameBoard.updateState(initialState)

      val result = initialState.inputHandler("play card:99", dummyController)

      result match {
        case Failure(msg) => msg should include ("Invalid card index")
        case _ => fail("Expected Failure but got Success or other")
      }
    }

    "fail inputHandler if card index is not a number" in {
      val result = initialState.inputHandler("play card:abc", dummyController)

      result match {
        case Failure(msg) => msg should include ("must be a digit")
        case _ => fail("Expected Failure but got Success or other")
      }
    }

    "undo invalid play and assign penalty card in inputHandler" in {
      val player1InitialHand = List(redDraw2)
      val player2InitialHand = List(blue5, yellow7)
      val initialState = new GameState(
        players = List(PlayerHand(player1InitialHand), PlayerHand(player2InitialHand)),
        currentPlayerIndex = 0,
        allCards = List(redDraw2, blue5, yellow7, green9),
        isReversed = false,
        discardPile = List(blue5),
        drawPile = List(blue5, yellow7),
        selectedColor = None,
        currentPhase = None
      )

      GameBoard.updateState(initialState)

      val result = initialState.inputHandler("play card:0", dummyController)

      result match {
        case Failure(msg) =>
          msg should include("Invalid play")

          val updatedState = dummyController.gameState.get

          updatedState.players.head.cards shouldBe List(green9, redDraw2)
          updatedState.players(1).cards shouldBe player2InitialHand
          updatedState.currentPlayerIndex shouldBe 0
          updatedState.drawPile shouldBe List(yellow7)

        case Success(_) =>
          fail("Expected Failure for invalid play but got Success")
      }
    }
  }
}
