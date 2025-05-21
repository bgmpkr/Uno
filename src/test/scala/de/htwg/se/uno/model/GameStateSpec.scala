package de.htwg.se.uno.model

import de.htwg.se.uno.controller.GameBoard
import de.htwg.se.uno.util.*
import de.htwg.se.uno.model.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class GameStateSpec extends AnyWordSpec {

  "GameState" should {

    val dummyGameState: GameState = GameState(
      players = List(),
      currentPlayerIndex = 0,
      allCards = List(),
      isReversed = false,
      discardPile = List(),
      drawPile = List()
    )

    val redOne = NumberCard("red", 1)
    val redTwo = NumberCard("red", 2)
    val blueOne = NumberCard("blue", 1)
    val skipRed = ActionCard("red", "skip")
    val reverseBlue = ActionCard("blue", "reverse")
    val drawTwoGreen = ActionCard("green", "draw two")
    val wild = WildCard("wild")
    val wildDrawFour = WildCard("wild draw four")

    def baseState(players: Int = 3): GameState = {
      GameState(
        players = List.fill(players)(PlayerHand(List.empty, false)),
        currentPlayerIndex = 0,
        allCards = List(redOne, redTwo, blueOne, skipRed, reverseBlue, drawTwoGreen, wild, wildDrawFour),
        discardPile = List(redOne),
        drawPile = List(redTwo, blueOne, skipRed, reverseBlue, drawTwoGreen, wild, wildDrawFour)
      )
    }

    //nextPlayer
    "return the previous player when isReversed is true" in {
      val player1 = PlayerHand(List(), hasSaidUno = false)
      val player2 = PlayerHand(List(), hasSaidUno = false)
      val player3 = PlayerHand(List(), hasSaidUno = false)

      val gameState = GameState(
        players = List(player1, player2, player3),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = true,
        discardPile = List(),
        drawPile = List()
      )

      val next = gameState.nextPlayer()

      next.currentPlayerIndex shouldBe 2
    }

    "return the next player when isReversed is false" in {
      val player1 = PlayerHand(List(), hasSaidUno = false)
      val player2 = PlayerHand(List(), hasSaidUno = false)
      val player3 = PlayerHand(List(), hasSaidUno = false)

      val gameState = GameState(
        players = List(player1, player2, player3),
        currentPlayerIndex = 1,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      val next = gameState.nextPlayer()

      next.currentPlayerIndex shouldBe 2
    }

    //checkForWinner
    "return Some(index) when a player has no cards" in {
      val winningHand = PlayerHand(List(), hasSaidUno = false)
      val otherHand = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)

      val gameState = GameState(
        players = List(otherHand, winningHand),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      gameState.checkForWinner() shouldBe Some(1)
    }

    "return None when no player has empty hand" in {
      val player1 = PlayerHand(List(NumberCard("red", 2)), hasSaidUno = false)
      val player2 = PlayerHand(List(NumberCard("blue", 9)), hasSaidUno = false)

      val gameState = GameState(
        players = List(player1, player2),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      gameState.checkForWinner() shouldBe None
    }

    //playerSaysUno
    "update the hasSaidUno status of the player to true" in {
      val player1 = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)
      val player2 = PlayerHand(List(NumberCard("blue", 7)), hasSaidUno = false)

      val gameState = GameState(
        players = List(player1, player2),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      val updatedGameState = gameState.playerSaysUno(0)

      updatedGameState.players(0).hasSaidUno shouldBe true
    }


    "drawCard" should {
      "throw exception when both draw and discard piles are too small" in {
        val player = PlayerHand(List(), hasSaidUno = false)
        val gameState = GameState(
          players = List(player),
          currentPlayerIndex = 0,
          allCards = List(),
          isReversed = false,
          discardPile = List(NumberCard("red", 3)),
          drawPile = List()
        )

        assertThrows[RuntimeException] {
          gameState.drawCard(player, List(), List(NumberCard("red", 3)))
        }
      }

      "reshuffle discard pile into draw pile if draw pile is empty" in {
        val player = PlayerHand(List(), hasSaidUno = false)
        val cardToDraw = NumberCard("blue", 5)
        val discardPile = List(cardToDraw, NumberCard("red", 3), NumberCard("green", 7))

        val gameState = GameState(
          players = List(player),
          currentPlayerIndex = 0,
          allCards = List(),
          isReversed = false,
          discardPile = discardPile,
          drawPile = List()
        )

        val (_, updatedHand, newDrawPile, _) = gameState.drawCard(player, List(), discardPile)
        updatedHand.cards.size shouldBe 1
        newDrawPile.size shouldBe 1
      }
    }

    //playCard
    "GameState.playCard" should {

      "return unchanged state if any player has empty hand" in {
        val state = baseState().copy(players = List(
          PlayerHand(List(redOne), false),
          PlayerHand(List.empty, false)
        ))
        state.playCard(redOne) shouldBe state
      }

      "stop drawing after max iterations" in {
        val state = baseState().copy(
          players = List(PlayerHand(List(blueOne), false)),
          drawPile = List.fill(10)(blueOne)
        )
        val result = state.playCard(blueOne)
        result.discardPile should contain(blueOne)
      }

      "update UNO status when player has UNO" in {
        val state = baseState().copy(
          players = List(PlayerHand(List(redOne), true))
        )
        val result = state.playCard(redOne)
        result.players.head.hasSaidUno shouldBe false
      }


      "handle skip card correctly by skipping the next player" in {
        val player1 = PlayerHand(List(skipRed), false)
        val player2 = PlayerHand(List(redOne), false)
        val player3 = PlayerHand(List(blueOne), false)

        val gameState = GameState(
          players = List(player1, player2, player3),
          currentPlayerIndex = 0,
          allCards = List(skipRed, redOne, blueOne),
          discardPile = List(redOne),
          drawPile = List()
        )

        val result = gameState.playCard(skipRed)
        result.currentPlayerIndex shouldBe 0// should skip player 1 (index 1)
      }

      "handle reverse card correctly by reversing direction" in {
        val player1 = PlayerHand(List(reverseBlue), false)
        val player2 = PlayerHand(List(redOne), false)

        val gameState = GameState(
          players = List(player1, player2),
          currentPlayerIndex = 0,
          allCards = List(reverseBlue, redOne),
          isReversed = false,
          discardPile = List(redOne),
          drawPile = List()
        )

        val result = gameState.playCard(reverseBlue)
        result.isReversed shouldBe false
        result.currentPlayerIndex shouldBe 0
      }


      "handle normal number card correctly by just moving to next player" in {
        val player1 = PlayerHand(List(redTwo), false)
        val player2 = PlayerHand(List(blueOne), false)

        val gameState = GameState(
          players = List(player1, player2),
          currentPlayerIndex = 0,
          allCards = List(redTwo, blueOne),
          discardPile = List(redOne),
          drawPile = List()
        )

        val result = gameState.playCard(redTwo)
        result.currentPlayerIndex shouldBe 0
        result.isReversed shouldBe false // direction unchanged
        result.discardPile should contain(redTwo)
      }
    }


    //isValidPlay
    "isValidPlay" should {
      "return true when topCard is None" in {
        val card = NumberCard("red", 5)
        val dummyPlayerHand = PlayerHand(List(card), hasSaidUno = false)
        val gameState = GameState(
          players = List(dummyPlayerHand),
          currentPlayerIndex = 0,
          allCards = List.empty,
          discardPile = List.empty,
          drawPile = List.empty
        )

        val result = gameState.isValidPlay(card, None)
        result shouldBe true
      }

      "return false when both cards are Wild Draw Four" in {
        val card = WildCard("wild draw four")
        val topCard = Some(WildCard("wild draw four"))
        dummyGameState.isValidPlay(card, topCard) shouldBe false
      }

      "return true for WildCard(wild) against any card" in {
        val card = WildCard("wild")
        val topCard = Some(NumberCard("red", 3))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true for WildCard(wild draw four) against any card" in {
        val card = WildCard("wild draw four")
        val topCard = Some(ActionCard("green", "reverse"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when any card is played on top of Wild(wild)" in {
        val card = NumberCard("yellow", 9)
        val topCard = Some(WildCard("wild"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when any card is played on top of Wild(wild draw four)" in {
        val card = ActionCard("blue", "skip")
        val topCard = Some(WildCard("wild draw four"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when Draw Two cards match by color" in {
        val card = ActionCard("red", "draw two")
        val topCard = Some(ActionCard("red", "draw two"))
        dummyGameState.isValidPlay(card, topCard) shouldBe false
      }

      "return true when NumberCards match by color" in {
        val card = NumberCard("green", 5)
        val topCard = Some(NumberCard("green", 9))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when NumberCards match by number" in {
        val card = NumberCard("blue", 7)
        val topCard = Some(NumberCard("red", 7))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when NumberCard is played on ActionCard with same color" in {
        val card = NumberCard("yellow", 4)
        val topCard = Some(ActionCard("yellow", "reverse"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when ActionCard is played on NumberCard with same color" in {
        val card = ActionCard("green", "skip")
        val topCard = Some(NumberCard("green", 2))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when ActionCards match by color" in {
        val card = ActionCard("blue", "skip")
        val topCard = Some(ActionCard("blue", "reverse"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return true when ActionCards match by action" in {
        val card = ActionCard("yellow", "skip")
        val topCard = Some(ActionCard("green", "skip"))
        dummyGameState.isValidPlay(card, topCard) shouldBe true
      }

      "return false when no rule matches" in {
        val card = NumberCard("green", 2)
        val topCard = Some(NumberCard("red", 9))
        dummyGameState.isValidPlay(card, topCard) shouldBe false
      }

      "reject a card with an invalid wildcard type and top card" in {
        val topCard = NumberCard("blue", 5)
        val invalidWildCard = WildCard("unknown")
        dummyGameState.isValidPlay(invalidWildCard, Some(topCard)) shouldBe false
      }

      "should trigger sayUno when player has one card left after playing a valid card" in {
        val startingCards = List(
          NumberCard("red", 3),
          NumberCard("blue", 5)
        )
        val playerHand = PlayerHand(startingCards)
        val gameState = GameState(
          players = List(playerHand),
          currentPlayerIndex = 0,
          allCards = Nil,
          isReversed = false,
          discardPile = List(NumberCard("red", 7)),
          drawPile = Nil
        )

        val updatedGameState = gameState.playCard(NumberCard("red", 3))
        val updatedPlayer = updatedGameState.players.head

        updatedPlayer.cards should have size 1
        updatedPlayer.hasSaidUno shouldBe false
      }
    }

    //notifyObservers
    "directly calling notifyObservers" should {
      "trigger observer update" in {
        var wasNotified = false

        val gameState = GameState(
          players = List.empty,
          currentPlayerIndex = 0,
          allCards = List.empty,
          discardPile = List.empty,
          drawPile = List.empty
        )

        gameState.addObserver(new Observer {
          override def update(): Unit = wasNotified = true
        })

        gameState.notifyObservers()

        wasNotified shouldBe true
      }
    }

    "dealInitialCards" should {
      "deal the correct number of cards to each player and update draw and discard piles accordingly" in {
        val playerCount = 3
        val cardsPerPlayer = 2

        val cards = List(
          NumberCard("red", 1),
          NumberCard("blue", 2),
          NumberCard("green", 3),
          NumberCard("yellow", 4),
          ActionCard("red", "skip"),
          WildCard("wild")
        )

        val players = List.fill(playerCount)(PlayerHand(List.empty, hasSaidUno = false))
        val initialState = GameState(
          players = players,
          currentPlayerIndex = 0,
          allCards = cards,
          isReversed = false,
          discardPile = List(),
          drawPile = cards
        )

        val afterDeal = initialState.dealInitialCards(cardsPerPlayer)

        afterDeal.players.foreach { playerHand =>
          playerHand.cards.size shouldBe cardsPerPlayer
        }

        afterDeal.drawPile.size shouldBe (cards.size - cardsPerPlayer * playerCount)

        afterDeal.discardPile shouldBe empty
      }
    }

    "handleDrawCards" should {
      "make the next player draw the correct number of cards when isReversed is false" in {
        val player1 = PlayerHand(List(NumberCard("red", 1)), false)
        val player2 = PlayerHand(List(NumberCard("blue", 2)), false)
        val player3 = PlayerHand(List(NumberCard("green", 3)), false)

        val drawCards = List(
          NumberCard("yellow", 4),
          NumberCard("yellow", 5),
          NumberCard("yellow", 6),
          NumberCard("yellow", 7)
        )

        val gameState = GameState(
          players = List(player1, player2, player3),
          currentPlayerIndex = 0,
          allCards = drawCards,
          isReversed = false,
          discardPile = List(NumberCard("red", 9)),
          drawPile = drawCards
        )

        val count = 3
        val updatedState = gameState.handleDrawCards(count)

        val updatedPlayer = updatedState.players(1)

        updatedPlayer.cards.size shouldBe (player2.cards.size + count)
        updatedState.drawPile.size shouldBe (drawCards.size - count)
        updatedState.discardPile shouldBe gameState.discardPile
      }

      "make the previous player draw the correct number of cards when isReversed is true" in {
        val player1 = PlayerHand(List(NumberCard("red", 1)), false)
        val player2 = PlayerHand(List(NumberCard("blue", 2)), false)
        val player3 = PlayerHand(List(NumberCard("green", 3)), false)

        val drawCards = List(
          NumberCard("yellow", 4),
          NumberCard("yellow", 5),
          NumberCard("yellow", 6),
          NumberCard("yellow", 7)
        )

        val gameState = GameState(
          players = List(player1, player2, player3),
          currentPlayerIndex = 0,
          allCards = drawCards,
          isReversed = true,
          discardPile = List(NumberCard("red", 9)),
          drawPile = drawCards
        )

        val count = 2
        val updatedState = gameState.handleDrawCards(count)

        val updatedPlayer = updatedState.players(2)

        updatedPlayer.cards.size shouldBe (player3.cards.size + count)
        updatedState.drawPile.size shouldBe (drawCards.size - count)
        updatedState.discardPile shouldBe gameState.discardPile
      }
    }

    "inputHandler" should {
      val wildCard = WildCard("wild")
      val normalCard = NumberCard("red", 5)
      val invalidCard = NumberCard("blue", 2)

      val player = PlayerHand(List(wildCard, normalCard, invalidCard), false)
      val initialState = GameState(
        players = List(player),
        currentPlayerIndex = 0,
        allCards = List.empty,
        isReversed = false,
        discardPile = List(NumberCard("green", 3)),
        drawPile = List.empty,
        selectedColor = None
      )
      "fail when playing wild card with invalid index" in {
        val result = initialState.inputHandler("play wild:10:red")
        result shouldBe a[Failure]
      }

      "fail when playing wild card on non-wild card" in {
        val result = initialState.inputHandler("play wild:1:red")
        result shouldBe a[Failure]
      }

      "play a valid normal card" in {
        val validState = initialState.copy(discardPile = List(NumberCard("red", 3)))
        val result = validState.inputHandler("play card:1")

        result match {
          case Success(newState) =>
            newState.players.head.cards.size shouldBe (player.cards.size - 1)
          case Failure(msg) =>
            fail(s"Unexpected failure: $msg")
        }
      }

      "fail when playing invalid normal card" in {
        val invalidState = initialState.copy(discardPile = List(NumberCard("green", 3)))
        val result = invalidState.inputHandler("play card:1")
        result shouldBe a[Failure]
      }

      "fail on unknown command" in {
        val result = initialState.inputHandler("foobar")
        result shouldBe a[Failure]
      }

      "fail if card index is not a number" in {
        val result = initialState.inputHandler("play card:abc")
        result shouldBe a[Failure]
      }

      "handle play wild command correctly and set selected color" in {
        val wildCard = WildCard("wild")
        val player = PlayerHand(List(wildCard), hasSaidUno = false)
        val gameState = GameState(
          players = List(player),
          currentPlayerIndex = 0,
          allCards = List(wildCard),
          isReversed = false,
          discardPile = List(),
          drawPile = List()
        )

        val result = gameState.inputHandler("play wild:0:red")

        result match {
          case Success(updatedGameState) =>
            updatedGameState.selectedColor shouldBe None
            updatedGameState.players(0).cards should not contain wildCard
          case Failure(msg) =>
            fail(s"Expected Success but got Failure: $msg")
        }
      }

      "handle invalid number format for wild card index" in {
        val result = initialState.inputHandler("play wild:abc:red")
        result shouldBe a[Failure]
        result.toString should include("Card index must be a number")
      }

      "handle invalid number format for normal card index" in {
        val result = initialState.inputHandler("play card:xyz")
        result shouldBe a[Failure]
        result.toString should include("Card index must be a number")
      }

      "handle negative wild card index" in {
        val result = initialState.inputHandler("play wild:-1:red")
        result shouldBe a[Failure]
        result.toString should include("Invalid card index")
      }

      "handle negative normal card index" in {
        val result = initialState.inputHandler("play card:-5")
        result shouldBe a[Failure]
        result.toString should include("Invalid card index")
      }

      "handle out-of-bounds wild card index" in {
        val result = initialState.inputHandler("play wild:99:red")
        result shouldBe a[Failure]
        result.toString should include("Invalid card index")
      }

      "handle out-of-bounds normal card index" in {
        val result = initialState.inputHandler("play card:99")
        result shouldBe a[Failure]
        result.toString should include("Invalid card index")
      }

      "isValidPlay should allow draw two on matching color number card" in {
        val drawTwoCard = ActionCard("red", "draw two")
        val numberCard = NumberCard("red", 5) // Matching color
        val gameState = GameState(
          players = List(PlayerHand(List(drawTwoCard))),
          currentPlayerIndex = 0,
          allCards = List(drawTwoCard, numberCard),
          isReversed = false,
          discardPile = List(numberCard),
          drawPile = Nil,
          selectedColor = None
        )

        gameState.isValidPlay(drawTwoCard, Some(numberCard)) shouldBe true
      }

      "isValidPlay should reject draw two on non-matching color number card" in {
        val drawTwoCard = ActionCard("red", "draw two")
        val numberCard = NumberCard("blue", 5) // Different color
        val gameState = GameState(
          players = List(PlayerHand(List(drawTwoCard))),
          currentPlayerIndex = 0,
          allCards = List(drawTwoCard, numberCard),
          isReversed = false,
          discardPile = List(numberCard),
          drawPile = Nil,
          selectedColor = None
        )

        gameState.isValidPlay(drawTwoCard, Some(numberCard)) shouldBe false
      }
      "GameBoard undo and redo commands" should {
        "correctly revert and reapply a state change" in {
          val handBefore = PlayerHand(List(NumberCard("red", 3)))
          val handAfter = PlayerHand(List())

          val dummyCard = NumberCard("red", 3)
          val discardPile = List(dummyCard)
          val drawPile = List(NumberCard("blue", 5), NumberCard("green", 7))

          val initialState = GameState(
            players = List(handBefore),
            currentPlayerIndex = 0,
            allCards = discardPile ++ drawPile,
            isReversed = false,
            discardPile = discardPile,
            drawPile = drawPile,
            selectedColor = Some("red")
          )

          val changedState = GameState(
            players = List(handAfter),
            currentPlayerIndex = 0,
            allCards = discardPile ++ drawPile,
            isReversed = false,
            discardPile = discardPile,
            drawPile = drawPile,
            selectedColor = Some("red")
          )

          GameBoard.initGame(initialState)

          class DummyStateChangeCommand(newState: GameState) extends Command {
            private var previousState: Option[GameState] = None

            override def execute(): Unit = {
              previousState = GameBoard.gameState.toOption
              GameBoard.updateState(newState)
            }

            override def undo(): Unit = {
              previousState.foreach(GameBoard.updateState)
            }

            override def redo(): Unit = {
              GameBoard.updateState(newState)
            }
          }

          val command = new DummyStateChangeCommand(changedState)

          GameBoard.executeCommand(command)
          assert(GameBoard.gameState.get.players.head.isEmpty)

          GameBoard.undoCommand()
          assert(!GameBoard.gameState.get.players.head.isEmpty)

          GameBoard.redoCommand()
          assert(GameBoard.gameState.get.players.head.isEmpty)
        }
      }
      
      "GameBoard isValidPlay" should {
        "return true for ActionCard matching selectedColor" in {
          val card = ActionCard("red", "skip")
          val topCard = NumberCard("blue", 5)
          val selectedColor = Some("red")

          val dummyState = GameState(
            players = List(PlayerHand(Nil)),
            currentPlayerIndex = 0,
            allCards = List(card, topCard),
            isReversed = false,
            discardPile = List(topCard),
            drawPile = Nil,
            selectedColor = selectedColor
          )

          GameBoard.initGame(dummyState)

          assert(GameBoard.isValidPlay(card, topCard, selectedColor))
        }

        "return true for NumberCard matching selectedColor" in {
          val card = NumberCard("red", 3)
          val topCard = NumberCard("blue", 5)
          val selectedColor = Some("red")

          val dummyState = GameState(
            players = List(PlayerHand(Nil)),
            currentPlayerIndex = 0,
            allCards = List(card, topCard),
            isReversed = false,
            discardPile = List(topCard),
            drawPile = Nil,
            selectedColor = selectedColor
          )

          GameBoard.initGame(dummyState)

          assert(GameBoard.isValidPlay(card, topCard, selectedColor))
        }

        "return true for WildCard when selectedColor is set" in {
          val card = WildCard("wild")
          val topCard = NumberCard("green", 2)
          val selectedColor = Some("red")

          val dummyState = GameState(
            players = List(PlayerHand(Nil)),
            currentPlayerIndex = 0,
            allCards = List(card, topCard),
            isReversed = false,
            discardPile = List(topCard),
            drawPile = Nil,
            selectedColor = selectedColor
          )

          GameBoard.initGame(dummyState)

          assert(GameBoard.isValidPlay(card, topCard, selectedColor))
        }

        "return true for ActionCard when selectedColor matches color but not top card" in {
          val playedCard = ActionCard("yellow", "reverse")
          val topCard = ActionCard("red", "skip") 
          val selectedColor = Some("yellow")

          val dummyState = GameState(
            players = List(PlayerHand(Nil)),
            currentPlayerIndex = 0,
            allCards = List(playedCard, topCard),
            isReversed = false,
            discardPile = List(topCard),
            drawPile = Nil,
            selectedColor = selectedColor
          )

          GameBoard.initGame(dummyState)

          assert(GameBoard.isValidPlay(playedCard, topCard, selectedColor))
        }
      }
    }
  }
}
