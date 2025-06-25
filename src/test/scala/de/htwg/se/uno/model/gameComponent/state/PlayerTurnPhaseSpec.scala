package de.htwg.se.uno.model.gameComponent.state

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{PlayerTurnPhase, UnoPhases}
import de.htwg.se.uno.model.gameComponent.base.state.*

class PlayerTurnPhaseSpec extends AnyWordSpec with Matchers {

  "PlayerTurnState" should {

    "call nextPlayer and return the current state" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List()) {
        override def nextPlayer(): GameState = this.copy(currentPlayerIndex = (currentPlayerIndex + 1) % 4)
      }

      val unoStates = new UnoPhases(dummyGameState)
      val state = PlayerTurnPhase(unoStates)
      unoStates.setState(state)

      val newState = state.nextPlayer()

      newState shouldBe state
      unoStates.gameState.currentPlayerIndex shouldBe 1
    }

    "return this on playCard" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      state.playCard(dummyCard) shouldBe state
    }

    "return this on drawCard" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      state.drawCard() shouldBe state
    }

    "return this on dealInitialCards" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      state.dealInitialCards() shouldBe state
    }

    "return this on checkForWinner" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      state.checkForWinner() shouldBe state
    }

    "return this on playerSaysUno" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      state.playerSaysUno() shouldBe state
    }

    "isValidPlay should be false" in {
      val unoStates = new UnoPhases(GameState(List(), 0, List(), false, List(), List()))
      val state = PlayerTurnPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      state.isValidPlay(dummyCard) shouldBe false
    }
  }
}
