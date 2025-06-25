package de.htwg.se.uno.model.gameComponent.state

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{GamePhase, UnoCalledPhase, UnoPhases}
import de.htwg.se.uno.model.playerComponent.PlayerHand

class UnoCalledPhaseSpec extends AnyWordSpec with Matchers {

  class DummyPlayerTurnState(context: UnoPhases) extends GamePhase {
    override def playCard(): GamePhase = this
    override def drawCard(): GamePhase = this
    override def nextPlayer(): GamePhase = this
    override def dealInitialCards(): GamePhase = this
    override def checkForWinner(): GamePhase = this
    override def playerSaysUno(): GamePhase = this
    override def isValidPlay: Boolean = true
  }

  "UnoCalledState" should {

    "handle playerSaysUno by updating gamestate and switching to PlayerTurnState" in {
      val dummyGameState = new GameState(
        players = List(PlayerHand(List()), PlayerHand(List())),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      ) {
        override def playerSaysUno(playerIndex: Int): GameState = {
          this
        }
      }

      val unoStates = new UnoPhases(dummyGameState)
      val unoCalledState = UnoCalledPhase(unoStates)

      unoStates.setState(unoCalledState)
      
      var setStateCalledWith: Option[GamePhase] = None
      val unoStatesSpy = new UnoPhases(dummyGameState) {
        override def setState(state: GamePhase): Unit = {
          setStateCalledWith = Some(state)
          super.setState(state)
        }
      }

      val unoCalledStateSpy = UnoCalledPhase(unoStatesSpy)
      unoStatesSpy.setState(unoCalledStateSpy)
      
      val resultState = unoCalledStateSpy.playerSaysUno()
      
      setStateCalledWith should not be None
      setStateCalledWith.get.getClass.getSimpleName shouldBe "PlayerTurnPhase"
      resultState shouldBe setStateCalledWith.get
    }
    
    "playCard should return this" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      state.playCard(dummyCard) shouldBe state
    }

    "drawCard should return this" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      state.drawCard() shouldBe state
    }

    "nextPlayer should return this" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      state.nextPlayer() shouldBe state
    }

    "dealInitialCards should return this" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      state.dealInitialCards() shouldBe state
    }

    "checkForWinner should return this" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      state.checkForWinner() shouldBe state
    }

    "isValidPlay should be false" in {
      val dummyGameState = new GameState(List(), 0, List(), false, List(), List())
      val unoStates = new UnoPhases(dummyGameState)
      val state = UnoCalledPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      state.isValidPlay(dummyCard) shouldBe false
    }
  }
}
