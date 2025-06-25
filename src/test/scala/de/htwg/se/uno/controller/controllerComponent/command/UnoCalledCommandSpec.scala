package de.htwg.se.uno.controller.controllerComponent.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.controller.controllerComponent.base.command.UnoCalledCommand
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{GameOverPhase, UnoPhases}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.Command

class UnoCalledCommandSpec extends AnyWordSpec with Matchers {

  val red5: NumberCard = NumberCard("red", 5)
  val blue5: NumberCard = NumberCard("blue", 5)
  val redDraw2: ActionCard = ActionCard("red", "draw two")
  val wildCard: WildCard = WildCard("wild")
  val drawFour: WildCard = WildCard("wild draw four")

  val allCards: List[Card] = List(red5, blue5, redDraw2, wildCard, drawFour)
  val player1: PlayerHand = PlayerHand(List(red5))
  val player2: PlayerHand = PlayerHand(List(blue5))
  val initialPlayers: List[PlayerHand] = List(player1, player2)

  val initialState: GameState = GameState(
    players = initialPlayers,
    currentPlayerIndex = 0,
    allCards = allCards,
    discardPile = List(redDraw2),
    drawPile = List(wildCard, red5, blue5),
    isReversed = false
  )
  val dummyController = new ControllerInterface {
    private var currentGameState: GameStateInterface = initialState
    override val fullDeck: List[Card] = allCards

    override def gameState: scala.util.Try[GameStateInterface] = scala.util.Success(currentGameState)

    override def startGame(players: Int, cardsPerPlayer: Int): Unit = {
      currentGameState = new GameState(
        players = List.fill(players)(PlayerHand(List.fill(cardsPerPlayer)(allCards.head))),
        currentPlayerIndex = 0,
        allCards = allCards,
        isReversed = false,
        discardPile = List(allCards.last),
        drawPile = allCards.drop(cardsPerPlayer * players + 1),
        selectedColor = None,
        currentPhase = None
      )
      notifyObservers()
    }

    override def updateState(newState: GameStateInterface): Unit = {
      currentGameState = newState
      notifyObservers()
    }

    override def initGame(state: GameStateInterface): Unit = updateState(state)

    override def undoCommand(): Unit = {
      notifyObservers()
    }

    override def redoCommand(): Unit = {
      notifyObservers()
    }

    override def executeCommand(cmd: Command): Unit = {
      cmd.execute()
      notifyObservers()
    }

    override def checkForWinner(): Option[Int] = {
      currentGameState.players.indexWhere(_.cards.isEmpty) match {
        case -1 => None
        case index => Some(index)
      }
    }

    override def isValidPlay(card: Card, topCard: Card, selectedColor: Option[String]): Boolean = {
      card.color == topCard.color ||
        selectedColor.contains(card.color)
    }
  }

  "UnoCalledCommand" should {

    "set GameOverPhase when current player has no cards and has said UNO" in {
      val player = PlayerHand(cards = List(), hasSaidUno = true)
      val players = List(player)

      val gameState = GameState(
        players = players,
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      GameBoard.updateState(gameState)

      val unoStates = new UnoPhases(GameBoard.gameState.get)
      unoStates.init()

      val command = UnoCalledCommand(dummyController)
      command.execute()
      unoStates.updateGameState(GameBoard.gameState.get)

      unoStates.checkForWinner()
      unoStates.state shouldBe a[GameOverPhase]
    }


    "update game state and switch to GameOverState if player has no cards and said UNO" in {
      val playerWithNoCards = PlayerHand(List(), hasSaidUno = true)
      val otherPlayer = PlayerHand(List(NumberCard("red", 5)))
      
      val initialState = GameState(
        players = List(playerWithNoCards, otherPlayer),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      GameBoard.updateState(initialState)

      val unoStates = new UnoPhases(GameBoard.gameState.get)
      unoStates.init()

      val command = UnoCalledCommand(dummyController)
      command.execute()
      unoStates.updateGameState(GameBoard.gameState.get)

      unoStates.checkForWinner()

      unoStates.state.getClass.getSimpleName shouldBe "GameOverPhase"
    }

    "update game state and not change state if player still has cards or hasn't said UNO" in {
      val playerWithCards = PlayerHand(List(NumberCard("red", 5)), hasSaidUno = false)
      val otherPlayer = PlayerHand(List(NumberCard("blue", 3)))

      val initialState = GameState(
        players = List(playerWithCards, otherPlayer),
        currentPlayerIndex = 0,
        allCards = List(),
        isReversed = false,
        discardPile = List(),
        drawPile = List()
      )

      GameBoard.updateState(initialState)

      val unoStates = new UnoPhases(initialState)
      unoStates.init()
      val command = UnoCalledCommand(dummyController)

      command.execute()
      
      unoStates.state.getClass.getSimpleName should not be "GameOverPhase"
    }
  }
}
