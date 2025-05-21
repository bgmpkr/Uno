package de.htwg.se.uno.controller

import de.htwg.se.uno.controller.command.DrawCardCommand
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.uno.model.*

class GameBoardSpec extends AnyWordSpec with Matchers {

  "GameBoard" should {

    "create a full deck with the expected number of cards" in {
      val deck = GameBoard.createDeckWithAllCards()
      deck.nonEmpty shouldBe true
      deck.count(_.isInstanceOf[WildCard]) shouldBe 8
    }

    "return Failure if gameState is not initialized" in {
      GameBoard.reset()
      GameBoard.gameState.isFailure shouldBe true
    }

    "allow initializing and retrieving gameState" in {
      val playerHand = PlayerHand(List(NumberCard("red", 5)))
      val initialState = GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = List(NumberCard("red", 5), WildCard("wild")),
        isReversed = false,
        drawPile = List(NumberCard("green", 2)),
        discardPile = List(NumberCard("red", 3)),
        selectedColor = None
      )

      GameBoard.initGame(initialState)

      GameBoard.gameState.isSuccess shouldBe true
      val state = GameBoard.gameState.get
      state.players should have size 1
      state.discardPile should not be empty
      state.drawPile should not be empty
      (state.discardPile ++ state.drawPile).toSet should equal(state.allCards.toSet)
    }

    "update the game state" in {
      val newPlayer = PlayerHand(List(NumberCard("green", 1)))
      val updatedState = GameBoard.gameState.get.copy(players = List(newPlayer))
      GameBoard.updateState(updatedState)
      GameBoard.gameState.get.players.head.cards.head shouldBe NumberCard("green", 1)
    }

    "check for winner correctly" in {
      val winner = PlayerHand(Nil).sayUno()
      val loser = PlayerHand(List(NumberCard("yellow", 3)))

      val topCard = NumberCard("red", 7)

      val initialState = GameState(
        players = List(loser, winner),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        drawPile = List(),
        discardPile = List(topCard),
        selectedColor = None
      )

      GameBoard.updateState(initialState)
      GameBoard.checkForWinner() shouldBe Some(1)
    }

    "reset the gameState" in {
      GameBoard.reset()
      GameBoard.gameState.isFailure shouldBe true
    }

    "return a valid shuffled discard and draw pile" in {
      val (discard, draw) = GameBoard.shuffleDeck()
      discard should not be empty
      draw should not be empty
      (discard ++ draw).size shouldEqual GameBoard.fullDeck.size
    }

    "validate a card play through proxy method" in {
      val validCard = NumberCard("red", 5)
      val topCard = NumberCard("red", 7)

      val initialState = GameState(
        players = List(PlayerHand(List(validCard))),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        drawPile = List(),
        discardPile = List(topCard),
        selectedColor = None
      )

      GameBoard.updateState(initialState)
      GameBoard.isValidPlay(validCard, topCard, None) shouldBe true
    }

    "call invoker.undoCommand when undoCommand is called" in {
      val command = DrawCardCommand()

      GameBoard.executeCommand(command)
      GameBoard.undoCommand()

      succeed
    }

    "call invoker.redoCommand when redoCommand is called" in {
      val command = DrawCardCommand()

      GameBoard.executeCommand(command)
      GameBoard.undoCommand()

      GameBoard.redoCommand()

      succeed
    }

    "return Some(index) when a player has an empty hand" in {
      val playerWithEmptyHand = PlayerHand(List())
      val playerWithCards = PlayerHand(List(Card("number")))

      val gameState = GameState(
        players = List(playerWithEmptyHand, playerWithCards),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List(),
        selectedColor = None
      )

      GameBoard.updateState(gameState)

      val winnerIndex = GameBoard.checkForWinner()
      assert(winnerIndex.contains(0))
    }

    "return None when no player has an empty hand" in {
      val player1 = PlayerHand(List(Card("number")))
      val player2 = PlayerHand(List(Card("number")))

      val gameState = GameState(
        players = List(player1, player2),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List(),
        selectedColor = None
      )

      GameBoard.updateState(gameState)

      val winnerIndex = GameBoard.checkForWinner()
      assert(winnerIndex.isEmpty)
    }
  }
}
