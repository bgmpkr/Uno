import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.controller.command.PlayCardCommand
import de.htwg.se.uno.controller.GameBoard
import de.htwg.se.uno.model._

class PlayCardCommandSpec extends AnyWordSpec with Matchers {

  val player1: PlayerHand = PlayerHand(List(NumberCard("red", 5), WildCard("wild")))
  val player2: PlayerHand = PlayerHand(List(NumberCard("green", 7)))

  val baseState: GameState = GameState(
    players = List(player1, player2),
    currentPlayerIndex = 0,
    allCards = Nil,
    isReversed = false,
    drawPile = List(NumberCard("yellow", 1), NumberCard("green", 4)),
    discardPile = List(NumberCard("red", 3)),
    selectedColor = None
  )

  "A PlayCardCommand" should {

    "play a valid number card and update state correctly" in {
      GameBoard.initGame(baseState)

      val command = PlayCardCommand(NumberCard("red", 5))
      command.execute()

      val updated = GameBoard.gameState.get
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

      GameBoard.updateState(invalidState)

      val command = PlayCardCommand(NumberCard("blue", 9))
      command.execute()

      val updated = GameBoard.gameState.get
      updated.players.head.cards should contain(NumberCard("blue", 9))
      updated.discardPile.head shouldBe NumberCard("red", 3)
    }

    "handle WildCard with color selection" in {
      val wildState = baseState.copy(
        players = List(PlayerHand(List(WildCard("wild"))), player2),
        discardPile = List(NumberCard("green", 4))
      )
      GameBoard.updateState(wildState)

      val command = PlayCardCommand(WildCard("wild"), Some("blue"))
      command.execute()

      val updated = GameBoard.gameState.get
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
      GameBoard.updateState(state)

      PlayCardCommand(ActionCard("red", "draw two")).execute()

      val updated = GameBoard.gameState.get
      updated.players(1).cards.size shouldBe 3
      updated.currentPlayerIndex shouldBe 0
    }

    "handle reverse correctly" in {
      val state = baseState.copy(
        players = List(PlayerHand(List(ActionCard("red", "reverse"))), player2),
        discardPile = List(NumberCard("red", 1)),
        isReversed = false
      )
      GameBoard.updateState(state)

      PlayCardCommand(ActionCard("red", "reverse")).execute()

      val updated = GameBoard.gameState.get
      updated.isReversed shouldBe true
      updated.currentPlayerIndex shouldBe 1
    }

    "handle wild draw four with color selection and drawing" in {
      val state = baseState.copy(
        players = List(PlayerHand(List(WildCard("wild draw four"))), player2),
        discardPile = List(NumberCard("yellow", 5)),
        drawPile = List(NumberCard("blue", 3), NumberCard("green", 8), NumberCard("yellow", 1), NumberCard("red", 9))
      )
      GameBoard.updateState(state)

      val command = PlayCardCommand(WildCard("wild draw four"), Some("green"))
      command.execute()

      val updated = GameBoard.gameState.get
      updated.selectedColor shouldBe Some("green")
      updated.players(1).cards.size shouldBe 5
      updated.currentPlayerIndex shouldBe 1
    }

    "cover undo and redo methods" in {
      val playerHand = PlayerHand(List(NumberCard("red", 3)))
      val initialState = GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = playerHand.cards,
        isReversed = false,
        discardPile = List(NumberCard("red", 5)),
        drawPile = List(),
        selectedColor = None
      )

      GameBoard.updateState(initialState)

      val cardToPlay = NumberCard("red", 3)
      val command = PlayCardCommand(cardToPlay)

      command.execute()

      command.undo()
      assert(GameBoard.gameState.isSuccess)

      command.redo()
      assert(GameBoard.gameState.isSuccess)
    }

    "cover skip action with nextPlayer twice" in {
      val skipCard = ActionCard("red", "skip")
      val playerHand = PlayerHand(List(skipCard))

      val initialState = GameState(
        players = List(playerHand, PlayerHand(List())),
        currentPlayerIndex = 0,
        allCards = playerHand.cards,
        isReversed = false,
        discardPile = List(ActionCard("red", "reverse")),
        drawPile = List(),
        selectedColor = None
      )

      GameBoard.updateState(initialState)

      val command = PlayCardCommand(skipCard)
      command.execute()

      val updatedState = GameBoard.gameState.get
      assert(updatedState.currentPlayerIndex == 0)
    }
  }
}
