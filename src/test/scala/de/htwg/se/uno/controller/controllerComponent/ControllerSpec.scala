package de.htwg.se.uno.controller.controllerComponent.base

import de.htwg.se.uno.model.cardComponent.{ActionCard, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.controller.controllerComponent.base.Controller
import de.htwg.se.uno.util.Command
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterEach

class ControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    Controller.updateState(testState)
    Controller.resetUndoRedo()
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
      Controller.initGame(testState)
      Controller.gameState.isSuccess shouldBe true
      val state = Controller.gameState.get
      state.drawPile.nonEmpty shouldBe true
      state.discardPile.size shouldBe 1
    }

    "update the internal game state and notify observers" in {
      val newState = testState.copy(currentPlayerIndex = 1)
      Controller.updateState(newState)
      Controller.gameState.get.currentPlayerIndex shouldBe 1
    }

    "create a full UNO deck" in {
      val deck = Controller.createDeckWithAllCards()
      deck.count(_.isInstanceOf[WildCard]) shouldBe 8
      deck.length should be > 90
    }

    "validate legal plays correctly via controller" in {
      val isValid = Controller.isValidPlay(redDraw2, red5, None)
      isValid shouldBe true

      val invalidPlay = Controller.isValidPlay(blue3, red5, None)
      invalidPlay shouldBe false
    }

    "execute and undo commands using invoker" in {
      var executed = false
      val testCommand = new Command {
        override def execute(): Unit = executed = true
        override def undo(): Unit = executed = false
        override def redo(): Unit = execute()
      }

      Controller.executeCommand(testCommand)
      executed shouldBe true

      Controller.undoCommand()
      executed shouldBe false
    }

    "reset the internal game state" in {
      Controller.reset()
      Controller.gameState.isFailure shouldBe true
    }

    "check if a player has won" in {
      val winningState = testState.copy(players = List(PlayerHand(Nil), testPlayer2))
      Controller.updateState(winningState)
      Controller.checkForWinner() shouldBe Some(0)
    }

  }
}
