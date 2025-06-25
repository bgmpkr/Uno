package de.htwg.se.uno.model.gameComponent.base.state

import de.htwg.se.uno.model.cardComponent.{Card, NumberCard}
import de.htwg.se.uno.model.gameComponent.{GameStateInterface, InputResult}
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*
import de.htwg.se.uno.model.gameComponent.base.state.GamePhaseJsonFormat.*
import de.htwg.se.uno.model.playerComponent.PlayerHand

class GamePhaseJsonFormatSpec extends AnyWordSpec with Matchers {

  val dummyCard: Card = NumberCard("red", 1)
  val dummyHand: PlayerHand = PlayerHand(List(dummyCard))

  val dummyGameState: GameStateInterface = new GameStateInterface {
    override val players: List[PlayerHand] = List(PlayerHand(List(NumberCard("red", 1))))
    override val currentPlayerIndex: Int = 0
    override val allCards: List[Card] = players.flatMap(_.cards)
    override val isReversed: Boolean = false
    override val discardPile: List[Card] = List(NumberCard("red", 1))
    override val drawPile: List[Card] = List(NumberCard("blue", 2))
    override val selectedColor: Option[String] = Some("red")
    val currentPhase: Option[GamePhase] = None

    override def drawCard(player: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
    (NumberCard, PlayerHand, List[Card], List[Card]) = (NumberCard("yellow", 3), player, drawPile, discardPile)
    def playCard(card: Card): GameStateInterface = this
    override def nextPlayer(): GameStateInterface = this
    override def copyWithPlayersAndPiles(players: List[PlayerHand], drawPile: List[Card], discardPile: List[Card]):
    GameStateInterface = this
    override def copyWithIsReversed(isReversed: Boolean): GameStateInterface = this
    override def isValidPlay(cardToPlay: Card, topCard: Option[Card], selectedColor: Option[String]) = true
    override def checkForWinner(): Option[Int] = None
    override def copyWithPiles(drawPile: List[Card], discardPile: List[Card]): GameStateInterface = this
    override def copyWithSelectedColor(selectedColor: Option[String]): GameStateInterface = this
    override def dealInitialCards(cardsPerPlayer: Int): GameStateInterface = this
    override def drawCardAndReturnDrawn(): (GameStateInterface, Card) = (this, NumberCard("green", 5))
    override def handleDrawCards(count: Int): GameStateInterface = this
    override def inputHandler(input: String, gameBoard: ControllerInterface): InputResult = {
        de.htwg.se.uno.model.gameComponent.Success(this)
      }
    override def playCard(card: Card, chosenColor: Option[String]): GameStateInterface = this
    override def playerSaysUno(playerIndex: Int): GameStateInterface = this
    override def setGameOver(): GameStateInterface = this
    override def setSelectedColor(color: String): GameStateInterface = this
  }


  object DummyContext extends UnoPhases(dummyGameState) {
    override def setState(state: GamePhase): Unit = super.setState(state)
  }

  "GamePhaseJsonFormat" should {

    "serialize DrawCardPhase correctly" in {
      val phase = DrawCardPhase(DummyContext)
      val json = Json.toJson(phase: GamePhase)
      json shouldBe JsString("DrawCardPhase")
    }

    "serialize PlayCardPhase correctly" in {
      val dummyCard = NumberCard("red", 1)
      val phase = PlayCardPhase(DummyContext, dummyCard)
      val json = Json.toJson(phase: GamePhase)
      json shouldBe JsString("PlayCardPhase")
    }

    "serialize GameOverPhase correctly" in {
      val phase = GameOverPhase()
      val json = Json.toJson(phase: GamePhase)
      json shouldBe JsString("GameOverPhase")
    }

    "deserialize DrawCardPhase correctly" in {
      val phase = DrawCardPhase(DummyContext)
      val json = Json.toJson(phase: GamePhase)
      val result = Json.toJson(phase: GamePhase)
      result shouldBe JsString("DrawCardPhase")
    }

    "deserialize PlayCardPhase correctly" in {
      val dummyCard = NumberCard("red", 1)
      val phase = PlayCardPhase(DummyContext, dummyCard)
      val result = Json.toJson(phase: GamePhase)
      result shouldBe JsString("PlayCardPhase")
    }

    "deserialize GameOverPhase correctly" in {
      val phase = GameOverPhase()
      val result = Json.toJson(phase: GamePhase)
      result shouldBe JsString("GameOverPhase")
    }

    "fail to deserialize unknown GamePhase" in {
      val result = Json.fromJson[GamePhase](JsString("UnknownPhase"))
      result.isError shouldBe true
    }
  }
}
