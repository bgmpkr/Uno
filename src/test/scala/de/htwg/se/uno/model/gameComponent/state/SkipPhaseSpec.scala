package de.htwg.se.uno.model.gameComponent.state

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.phase.{SkipPhase, UnoPhases}

class SkipPhaseSpec extends AnyWordSpec with Matchers {

  "SkipState" should {

    "call nextPlayer twice and change state to PlayerTurnPhase" in {
      val dummyGameState: GameState = new GameState(List(), 0, List(), false, List(), List()) {
        private var counter = 0
        override def nextPlayer(): GameState = {
          counter += 1
          if (counter < 2) this else this.copy(currentPlayerIndex = (currentPlayerIndex + 2) % 4)
        }
      }

      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)

      val resultState = skipState.nextPlayer()

      resultState.getClass.getSimpleName shouldBe "PlayerTurnPhase"
      unoStates.gameState.currentPlayerIndex shouldBe 2
    }

    "return this on playCard" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      skipState.playCard(dummyCard) shouldBe skipState
    }

    "return this on drawCard" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      skipState.drawCard() shouldBe skipState
    }

    "return this on dealInitialCards" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      skipState.dealInitialCards() shouldBe skipState
    }

    "return this on checkForWinner" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      skipState.checkForWinner() shouldBe skipState
    }

    "return this on playerSaysUno" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      skipState.playerSaysUno() shouldBe skipState
    }

    "isValidPlay should be false" in {
      val dummyGameState = GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val skipState = SkipPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      skipState.isValidPlay(dummyCard) shouldBe false
    }
  }
}
