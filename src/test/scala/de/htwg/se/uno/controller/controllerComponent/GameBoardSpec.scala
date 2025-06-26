package de.htwg.se.uno.controller.controllerComponent.base

import de.htwg.se.uno.model.cardComponent.{ActionCard, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.util.Command
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterEach

class GameBoardSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    GameBoard.updateState(testState)
    GameBoard.resetUndoRedo()
  }

  val red5: NumberCard = NumberCard("red", 5)
  val blue5: NumberCard = NumberCard("blue", 5)
  val blue3: NumberCard = NumberCard("blue", 3)
  val redDraw2: ActionCard = ActionCard("red", "draw two")
  val wild: WildCard = WildCard("wild")

  val testPlayer1: PlayerHand = PlayerHand(List(red5, redDraw2))
  val testPlayer2: PlayerHand = PlayerHand(List(blue5, wild))
  val testPlayers: List[PlayerHand] = List(testPlayer1, testPlayer2)

  val testState: GameState = GameState(
    players = testPlayers,
    currentPlayerIndex = 0,
    allCards = Nil,
    discardPile = List(red5),
    drawPile = List(wild, redDraw2),
    isReversed = false
  )

  "GameBoard" should {

    "initialize a new game state with a shuffled deck" in {
      GameBoard.initGame(testState)
      GameBoard.gameState.isSuccess shouldBe true
      val state = GameBoard.gameState.get
      state.drawPile.nonEmpty shouldBe true
      state.discardPile.size shouldBe 1
    }

    "update the internal game state and notify observers" in {
      val newState = testState.copy(currentPlayerIndex = 1)
      GameBoard.updateState(newState)
      GameBoard.gameState.get.currentPlayerIndex shouldBe 1
    }

    "create a full UNO deck" in {
      val deck = GameBoard.createDeckWithAllCards()
      deck.count(_.isInstanceOf[WildCard]) shouldBe 8
      deck.length should be > 90
    }

    "validate legal plays correctly via controller" in {
      val isValid = GameBoard.isValidPlay(redDraw2, red5, None)
      isValid shouldBe true

      val invalidPlay = GameBoard.isValidPlay(blue3, red5, None)
      invalidPlay shouldBe false
    }

    "execute and undo commands using invoker" in {
      var executed = false
      val testCommand = new Command {
        override def execute(): Unit = executed = true
        override def undo(): Unit = executed = false
        override def redo(): Unit = execute()
      }

      GameBoard.executeCommand(testCommand)
      executed shouldBe true

      GameBoard.undoCommand()
      executed shouldBe false
    }

    "reset the internal game state" in {
      GameBoard.reset()
      GameBoard.gameState.isFailure shouldBe true
    }

    "check if a player has won" in {
      val winningState = testState.copy(players = List(PlayerHand(Nil), testPlayer2))
      GameBoard.updateState(winningState)
      GameBoard.checkForWinner() shouldBe Some(0)
    }

  }
}
