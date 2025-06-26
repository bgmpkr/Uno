package de.htwg.se.uno.controller.controllerComponent.command

import de.htwg.se.uno.controller.controllerComponent.base.Controller
import de.htwg.se.uno.controller.controllerComponent.base.command.PlayCardCommand
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.playerComponent.PlayerHand
import org.scalatest.BeforeAndAfterEach

class PlayCardCommandSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    Controller.updateState(baseState)
    Controller.resetUndoRedo()
  }

  val controller = Controller

  val player1: PlayerHand = PlayerHand(List(NumberCard("red", 5), WildCard("wild")))
  val player2: PlayerHand = PlayerHand(List(NumberCard("green", 7)))

  val baseState: GameState = GameState(
    players = List(player1, player2),
    currentPlayerIndex = 0,
    allCards = Nil,
    isReversed = false,
    drawPile = List(NumberCard("yellow", 1), NumberCard("green", 4)),
    discardPile = List(NumberCard("red", 5)),
    selectedColor = None
  )

  def cmd(card: Card, chooseColor: Option[String] = None): PlayCardCommand =
    PlayCardCommand(card, chooseColor, controller)

  "A PlayCardCommand" should {

    "play a valid number card and update state correctly" in {
      controller.initGame(baseState)

      val command = cmd(NumberCard("red", 5))
      command.execute()

      val updated = controller.gameState.get
      updated.players.head.cards should not contain NumberCard("red", 5)
      updated.discardPile.head shouldBe NumberCard("red", 5)
      updated.currentPlayerIndex shouldBe 1
    }

    "not allow an invalid card to be played" in {
      val invalidState = baseState.copy(
        players = List(PlayerHand(List(NumberCard("blue", 9))), player2),
        discardPile = List(NumberCard("red", 3)),
        drawPile = List()
      )

      controller.updateState(invalidState)

      val command = cmd(NumberCard("blue", 9))
      command.execute()

      val updated = controller.gameState.get
      updated.players.head.cards should contain(NumberCard("blue", 9))
      updated.discardPile.head shouldBe NumberCard("red", 3)
    }

    "handle WildCard with color selection" in {
      val wildState = baseState.copy(
        players = List(PlayerHand(List(WildCard("wild"))), player2),
        discardPile = List(NumberCard("green", 4))
      )
      controller.updateState(wildState)

      val command = cmd(WildCard("wild"), Some("blue"))
      command.execute()

      val updated = controller.gameState.get
      updated.discardPile.head shouldBe WildCard("wild")
      updated.selectedColor shouldBe Some("blue")
      updated.players.head.cards.exists(_.isInstanceOf[WildCard]) shouldBe false
    }

    "handle draw two and skip next player" in {
      val state = baseState.copy(
        players = List(PlayerHand(List(ActionCard("red", "draw two"))), player2),
        discardPile = List(NumberCard("red", 1)),
        drawPile = List(NumberCard("yellow", 3), NumberCard("green", 2), NumberCard("blue", 5))
      )
      controller.updateState(state)

      cmd(ActionCard("red", "draw two")).execute()

      val updated = controller.gameState.get
      updated.players(1).cards.size shouldBe 3
      updated.currentPlayerIndex shouldBe 0
    }

    "handle reverse correctly" in {
      val state = baseState.copy(
        players = List(PlayerHand(List(ActionCard("red", "reverse"))), player2),
        discardPile = List(NumberCard("red", 1)),
        isReversed = false
      )
      controller.updateState(state)

      cmd(ActionCard("red", "reverse")).execute()

      val updated = controller.gameState.get
      updated.currentPlayerIndex shouldBe 1
    }

    "handle wild draw four with color selection and drawing" in {
      val state = baseState.copy(
        players = List(PlayerHand(List(WildCard("wild draw four"))), player2),
        discardPile = List(NumberCard("yellow", 5)),
        drawPile = List(
          NumberCard("blue", 3),
          NumberCard("green", 8),
          NumberCard("yellow", 1),
          NumberCard("red", 9)
        )
      )
      controller.updateState(state)

      val command = cmd(WildCard("wild draw four"), Some("green"))
      command.execute()

      val updated = controller.gameState.get
      updated.players(1).cards.size shouldBe 5
      updated.currentPlayerIndex shouldBe 1
    }
  }
}
