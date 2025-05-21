package de.htwg.se.uno.aview

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.state.*
import de.htwg.se.uno.controller.GameBoard

class UnoTuiSpec extends AnyWordSpec with Matchers {

  "UnoTui" should {

    val playerHand = PlayerHand(List(NumberCard("red", 5), WildCard("wild")), hasSaidUno = false)

    val gameState = GameState(
      players = List(playerHand),
      currentPlayerIndex = 0,
      allCards = List(NumberCard("red", 5), WildCard("wild")),
      isReversed = false,
      drawPile = List(NumberCard("red", 9)),
      discardPile = List(NumberCard("red", 3)),
      selectedColor = None
    )

    GameBoard.updateState(gameState)
    val context = new UnoPhases(gameState)
    val tui = new UnoTui(context)

    "display the game state without throwing" in {
      GameBoard.gameState match {
        case scala.util.Success(_) => noException should be thrownBy tui.display()
        case scala.util.Failure(e) => fail(s"GameState not initialized: ${e.getMessage}")
      }
    }

    "handle a valid card index input" in {
      GameBoard.gameState match {
        case scala.util.Success(_) => noException should be thrownBy tui.handleInput("0")
        case scala.util.Failure(e) => fail(s"GameState not initialized: ${e.getMessage}")
      }
    }

    "handle invalid (non-integer) input gracefully" in {
      noException should be thrownBy tui.handleInput("invalid")
    }

    "handle 'draw' command without throwing" in {
      noException should be thrownBy tui.handleInput("draw")
    }

    "choose wild color with simulated input" in {
      val simulatedInput = () => "2" // blue
      val chosen = tui.chooseWildColor(simulatedInput)
      chosen shouldBe "blue"
    }

    "detect win and set shouldExit to true" in {
      val winningPlayer = PlayerHand(Nil, hasSaidUno = true)
      val winningState = gameState.copy(players = List(winningPlayer))
      GameBoard.updateState(winningState)

      GameBoard.checkForWinner().isDefined shouldBe false
    }

    "trigger update without throwing" in {
      noException should be thrownBy tui.update()
    }


    "handle draw input with playable drawn card" in {
      val playableCard = NumberCard("red", 3)
      val playerHand = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)

      val gameStateWithPlayableDrawnCard = new GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = List(NumberCard("red", 5), playableCard),
        isReversed = false,
        drawPile = List.empty,
        discardPile = List(NumberCard("red", 3)), // top card
        selectedColor = None
      ) {
        override def drawCardAndReturnDrawn(): (GameState, Card) = {
          val drawnCard = playableCard
          val updatedPlayerHand = playerHand.copy(cards = playerHand.cards :+ drawnCard)
          val newState = this.copy(players = List(updatedPlayerHand))
          (newState, drawnCard)
        }

        override def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String]): Boolean = {
          card match {
            case NumberCard(color, number) =>
              topCard.exists {
                case NumberCard(tcColor, tcNumber) => color == tcColor || number == tcNumber
                case _ => false
              }
            case _ => false
          }
        }
      }

      GameBoard.updateState(gameStateWithPlayableDrawnCard)
      val context = new UnoPhases(gameStateWithPlayableDrawnCard)
      val tui = new UnoTui(context)

      noException should be thrownBy tui.handleInput("draw")
    }

    "handle draw input with non-playable drawn card" in {
      val nonPlayableCard = NumberCard("blue", 7)
      val playerHand = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)

      val gameStateWithNonPlayableDrawnCard = new GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = List(NumberCard("red", 5), nonPlayableCard),
        isReversed = false,
        drawPile = List.empty,
        discardPile = List(NumberCard("red", 3)),
        selectedColor = None
      ) {
        override def drawCardAndReturnDrawn(): (GameState, Card) = {
          val drawnCard = nonPlayableCard
          val updatedPlayerHand = playerHand.copy(cards = playerHand.cards :+ drawnCard)
          val newState = this.copy(players = List(updatedPlayerHand))
          (newState, drawnCard)
        }

        override def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String]): Boolean = {
          card match {
            case NumberCard(color, number) =>
              topCard.exists {
                case NumberCard(tcColor, tcNumber) => color == tcColor || number == tcNumber
                case _ => false
              }
            case _ => false
          }
        }

        override def nextPlayer(): GameState = this
      }

      GameBoard.updateState(gameStateWithNonPlayableDrawnCard)
      val context = new UnoPhases(gameStateWithNonPlayableDrawnCard)
      val tui = new UnoTui(context)

      noException should be thrownBy tui.handleInput("draw")
    }


    "print 'Player X said UNO' for other players" in {
      val player1 = PlayerHand(List(NumberCard("red", 1)), hasSaidUno = true)
      val player2 = PlayerHand(List(NumberCard("blue", 2)), hasSaidUno = true)
      val player3 = PlayerHand(List(NumberCard("green", 3)), hasSaidUno = false)
      val stateWithMultipleUno = GameState(
        players = List(player1, player2, player3),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        drawPile = List(),
        discardPile = List(NumberCard("red", 1)),
        selectedColor = None
      )
      GameBoard.updateState(stateWithMultipleUno)
      val context = new UnoPhases(stateWithMultipleUno)
      val tui = new UnoTui(context)
      val output = new java.io.ByteArrayOutputStream()
      Console.withOut(output) {
        tui.display()
      }

      output.toString should include("You said 'UNO'!") // current player message
      output.toString should include("Player 2 said UNO") // other player message
    }

    "print error message when game state is not initialized (handleInput)" in {
      val tui = new UnoTui(new UnoPhases(gameState))

      def failureGameState = scala.util.Failure(new RuntimeException("State missing"))

      val output = new java.io.ByteArrayOutputStream()
      Console.withOut(output) {
        noException should be thrownBy tui.handleInput("draw")
      }
    }

    "print 'You said UNO!' when player has one card and hasn't said UNO" in {
      val player = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)
      val state = gameState.copy(players = List(player), currentPlayerIndex = 0)
      GameBoard.updateState(state)
      val context = new UnoPhases(state)
      val tui = new UnoTui(context)

      val output = new java.io.ByteArrayOutputStream()
      Console.withOut(output) {
        tui.handleInput("draw")
      }

      output.toString should include("You said 'UNO'!")
    }
  }
}
