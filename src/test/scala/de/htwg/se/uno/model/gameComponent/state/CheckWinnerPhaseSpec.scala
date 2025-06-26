package de.htwg.se.uno.model.gameComponent.state

import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.phase.{CheckWinnerPhase, GameOverPhase, PlayerTurnPhase, UnoPhases}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CheckWinnerPhaseSpec extends AnyWordSpec with Matchers {

  class GameStateWithWinner(players: List[PlayerHand]) extends GameState(
    players = players,
    currentPlayerIndex = 0,
    allCards = List(),
    isReversed = false,
    discardPile = List(),
    drawPile = List()
  ) {
    override def checkForWinner(): Option[Int] = Some(0)
  }

  class GameStateWithoutWinner(players: List[PlayerHand]) extends GameState(
    players = players,
    currentPlayerIndex = 0,
    allCards = List(),
    isReversed = false,
    discardPile = List(),
    drawPile = List()
  ) {
    override def checkForWinner(): Option[Int] = None
  }

  "CheckWinnerState" should {

    "transition to GameOverState if there is a winner" in {
      val players = List(
        PlayerHand(List(NumberCard("red", 5)))
      )
      val gameState = new GameStateWithWinner(players)
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)

      unoStates.setState(checkWinnerState)
      val newState = unoStates.state.checkForWinner()

      newState shouldBe a [GameOverPhase]
    }

    "transition to PlayerTurnState if there is no winner" in {
      val players = List(
        PlayerHand(List(NumberCard("red", 5)))
      )
      val gameState = new GameStateWithoutWinner(players)
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)

      unoStates.setState(checkWinnerState)
      val newState = unoStates.state.checkForWinner()

      newState shouldBe a [PlayerTurnPhase]
    }

    "return itself when playCard is called" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      val dummyCard = NumberCard("green", 5)
      unoStates.setState(checkWinnerState)

      val result = checkWinnerState.playCard(dummyCard)
      result shouldBe checkWinnerState
    }

    "return itself when drawCard is called" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      unoStates.setState(checkWinnerState)

      val result = checkWinnerState.drawCard()
      result shouldBe checkWinnerState
    }

    "return itself when nextPlayer is called" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      unoStates.setState(checkWinnerState)

      val result = checkWinnerState.nextPlayer()
      result shouldBe checkWinnerState
    }

    "return itself when dealInitialCards is called" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      unoStates.setState(checkWinnerState)

      val result = checkWinnerState.dealInitialCards()
      result shouldBe checkWinnerState
    }

    "return itself when playerSaysUno is called" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      unoStates.setState(checkWinnerState)

      val result = checkWinnerState.playerSaysUno()
      result shouldBe checkWinnerState
    }

    "return false for isValidPlay" in {
      val gameState = new GameStateWithoutWinner(List())
      val unoStates = new UnoPhases(gameState)
      val dummyCard = NumberCard("green", 5)
      val checkWinnerState = CheckWinnerPhase(unoStates)
      unoStates.setState(checkWinnerState)

      checkWinnerState.isValidPlay(dummyCard) shouldBe false
    }
  }
}
