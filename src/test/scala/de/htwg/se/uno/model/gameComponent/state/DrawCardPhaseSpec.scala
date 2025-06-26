package de.htwg.se.uno.model.gameComponent.state

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{Card, NumberCard}
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{DrawCardPhase, UnoPhases}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.model.gameComponent.base.state.*

class DrawCardPhaseSpec extends AnyWordSpec with Matchers {

  "DrawCardState" should {

    "perform drawCard and update gameState and state correctly" in {
      val initialHand = PlayerHand(List(NumberCard("red", 5)))
      val drawPile = List(NumberCard("green", 7), NumberCard("yellow", 3))
      val discardPile = List(NumberCard("blue", 2))

      val dummyGameState = new GameState(
        players = List(initialHand, PlayerHand(Nil)),
        currentPlayerIndex = 0,
        allCards = Nil,
        isReversed = false,
        discardPile = discardPile,
        drawPile = drawPile
      ) {
        override def drawCard(player: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
        (Option[Card], PlayerHand, List[Card], List[Card]) = {
          val drawnCard = drawPile.head
          val newHand = player + drawnCard
          val newDrawPile = drawPile.tail
          (Some(drawnCard), newHand, newDrawPile, discardPile)
        }
      }

      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)

      val newState = drawCardState.drawCard()

      newState.getClass.getSimpleName shouldBe "PlayerTurnPhase"

      val updatedHand = unoStates.gameState.players.head
      updatedHand.cards should contain(NumberCard("green", 7))

      unoStates.gameState.drawPile should have size (drawPile.size - 1)
      unoStates.gameState.discardPile shouldBe discardPile
    }

    "return this on playCard" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val dummyCard = NumberCard("green", 5)
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      drawCardState.playCard(dummyCard) shouldBe drawCardState
    }

    "return this on nextPlayer" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      drawCardState.nextPlayer() shouldBe drawCardState
    }

    "return this on dealInitialCards" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      drawCardState.dealInitialCards() shouldBe drawCardState
    }

    "return this on checkForWinner" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      drawCardState.checkForWinner() shouldBe drawCardState
    }

    "return this on playerSaysUno" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      drawCardState.playerSaysUno() shouldBe drawCardState
    }

    "isValidPlay should be false" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val drawCardState = DrawCardPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      drawCardState.isValidPlay(dummyCard) shouldBe false
    }
  }
}
