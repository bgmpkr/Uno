package de.htwg.se.uno.model.state

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{PlayerTurnPhase, ReversePhase, UnoPhases}
import de.htwg.se.uno.model.playerComponent.PlayerHand

class ReversePhaseSpec extends AnyWordSpec with Matchers {

  "ReverseState" should {

    "toggle isReversed and move to the next player, then switch to PlayerTurnState" in {
      val initialState = GameState(
        players = List(PlayerHand(Nil), PlayerHand(Nil), PlayerHand(Nil)),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      val unoStates = new UnoPhases(initialState)
      val reverseState = ReversePhase(unoStates)
      val result = reverseState.nextPlayer()

      result shouldBe a[PlayerTurnPhase]
      unoStates.gameState.isReversed shouldBe true
      unoStates.gameState.currentPlayerIndex should not be 0
    }

    "return this on playCard" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.playCard() shouldBe reverseState
    }

    "return this on drawCard" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.drawCard() shouldBe reverseState
    }

    "return this on dealInitialCards" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.dealInitialCards() shouldBe reverseState
    }

    "return this on checkForWinner" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.checkForWinner() shouldBe reverseState
    }

    "return this on playerSaysUno" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.playerSaysUno() shouldBe reverseState
    }

    "return false for isValidPlay" in {
      val unoStates = new UnoPhases(GameState(Nil, 0, Nil, false, Nil, Nil))
      val reverseState = ReversePhase(unoStates)
      reverseState.isValidPlay shouldBe false
    }
  }
}
