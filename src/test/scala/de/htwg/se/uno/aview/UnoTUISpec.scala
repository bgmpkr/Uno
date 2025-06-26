package de.htwg.se.uno.aview

import de.htwg.se.uno.controller.controllerComponent.base.Controller
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.phase.UnoPhases
import de.htwg.se.uno.model.playerComponent.PlayerHand

class UnoTUISpec extends AnyWordSpec with Matchers {

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

    Controller.updateState(gameState)
    val context = new UnoPhases(gameState)
    val tui = new UnoTUI(Controller)

    "display the game state without throwing" in {
      Controller.gameState match {
        case scala.util.Success(_) => noException should be thrownBy tui.display()
        case scala.util.Failure(e) => fail(s"GameState not initialized: ${e.getMessage}")
      }
    }

    "handle a valid card index input" in {
      Controller.gameState match {
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
      val simulatedInput = () => "2"
      val chosen = tui.chooseWildColor(simulatedInput)
      chosen shouldBe "blue"
    }

    "detect win and set shouldExit to true" in {
      val winningPlayer = PlayerHand(Nil, hasSaidUno = true)
      val dummyLoser    = PlayerHand(List(NumberCard("red", 3)))
      val winningState = gameState.copy(players = List(winningPlayer, dummyLoser), currentPlayerIndex = 0)
      Controller.updateState(winningState)

      Controller.checkForWinner() shouldBe Some(0)
    }

    "trigger update without throwing" in {
      noException should be thrownBy tui.update()
    }


    "handle 'undo' command without throwing" in {
      noException should be thrownBy tui.handleInput("undo")
    }

    "handle 'redo' command without throwing" in {
      noException should be thrownBy tui.handleInput("redo")
    }

    "handle out-of-range index input" in {
      noException should be thrownBy tui.handleInput("99")
    }

    "handle 'draw' when drawn card is not playable" in {
      val unplayableCard = NumberCard("green", 7)
      val currentCard = NumberCard("red", 3)

      val state = gameState.copy(
        drawPile = List(unplayableCard),
        discardPile = List(currentCard, currentCard, currentCard, currentCard, currentCard),
        players = List(PlayerHand(Nil, hasSaidUno = false))
      )

      Controller.updateState(state)

      noException should be thrownBy tui.handleInput("draw")
    }


    "handle playing a WildCard via index input" in {
      val hand = PlayerHand(List(WildCard("wild")), hasSaidUno = false)
      val state = gameState.copy(
        players = List(hand),
        discardPile = List(NumberCard("red", 3))
      )
      Controller.updateState(state)

      val tui = new UnoTUI(Controller)

      val simulatedInput = Iterator("2")
      noException should be thrownBy tui.handleInput("0", () => simulatedInput.next())
    }



    "not fail when discard pile is empty" in {
      val emptyDiscardState = gameState.copy(discardPile = Nil)
      Controller.updateState(emptyDiscardState)
      noException should be thrownBy tui.display()
    }


    "display UNO message for other players" in {
      val stateWithUnoPlayer = gameState.copy(players = List(
        PlayerHand(List(NumberCard("red", 1)), hasSaidUno = false),
        PlayerHand(List(NumberCard("blue", 2)), hasSaidUno = true)
      ), currentPlayerIndex = 0)

      Controller.updateState(stateWithUnoPlayer)
      noException should be thrownBy tui.display()
    }


    "display selected color if it exists" in {
      tui.selectedColor = Some("blue")
      noException should be thrownBy tui.display()
    }

    "display error message when game state is not initialized" in {
      Controller.reset()
      val tuiNew = new UnoTUI(Controller)
      noException should be thrownBy tuiNew.display()
    }

    "handle input gracefully when game state is not initialized" in {
      Controller.reset()
      val tuiNew = new UnoTUI(Controller)
      noException should be thrownBy tuiNew.handleInput("draw")
    }

    "print wild card color options when choosing" in {
      val input = Iterator("1")
      val simulatedInput = () => input.next()
      noException should be thrownBy tui.chooseWildColor(simulatedInput)
    }


    "display the player's hand with indices" in {
      val player = PlayerHand(List(NumberCard("red", 4), NumberCard("green", 2)))
      noException should be thrownBy tui.display()
    }


    "print winning message when a player has won" in {
      val winningPlayer = PlayerHand(Nil, hasSaidUno = true)
      val winningState = gameState.copy(players = List(winningPlayer))
      Controller.updateState(winningState)
      noException should be thrownBy tui.checkForWinner()
    }

    "set and get the shouldExit flag correctly" in {
      tui.setShouldExit(true)
      tui.shouldExit shouldBe true
    }

    "handle exception when checking UNO with uninitialized game state" in {
      Controller.reset()
      val tuiNew = new UnoTUI(Controller)
      noException should be thrownBy tui.checkForWinner()
    }

    "handle drawing a non-playable card by skipping turn" in {
      val unplayableCard = NumberCard("green", 9)
      val hand = PlayerHand(Nil)
      val drawPile = List(unplayableCard)
      val discardPile = List(NumberCard("red", 3))

      val gameState = GameState(
        players = List(hand),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        drawPile = drawPile,
        discardPile = discardPile,
        selectedColor = None
      )

      Controller.updateState(gameState)
      val tui = new UnoTUI(Controller)

      noException should be thrownBy tui.handleInput("draw")
    }

    "print error when game state is not initialized in checkUno" in {
      Controller.reset()
      val tui = new UnoTUI(Controller)
      noException should be thrownBy tui.checkForWinner()
    }


  }
}
