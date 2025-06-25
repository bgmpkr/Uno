package de.htwg.se.uno.model.gameComponent.state

import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{GameOverPhase, UnoPhases}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameOverPhaseSpec extends AnyWordSpec with Matchers {

  "GameOverState" should {

    val dummyGameState = GameState(Nil, 0, Nil, false, Nil, Nil)
    val unoStates = new UnoPhases(dummyGameState)
    val gameOverState = GameOverPhase()

    "return this on playCard" in {
      val dummyCard = NumberCard("green", 5)
      gameOverState.playCard(dummyCard) shouldBe gameOverState
    }

    "return this on drawCard" in {
      gameOverState.drawCard() shouldBe gameOverState
    }

    "return this on nextPlayer" in {
      gameOverState.nextPlayer() shouldBe gameOverState
    }

    "return this on dealInitialCards" in {
      gameOverState.dealInitialCards() shouldBe gameOverState
    }

    "return this on checkForWinner" in {
      gameOverState.checkForWinner() shouldBe gameOverState
    }

    "return this on playerSaysUno" in {
      gameOverState.playerSaysUno() shouldBe gameOverState
    }

    "have isValidPlay false" in {
      val dummyCard = NumberCard("green", 5)
      gameOverState.isValidPlay(dummyCard) shouldBe false
    }
  }
}
