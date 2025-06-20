package de.htwg.se.uno.model.gameComponent.mock

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.cardComponent.{Card, WildCard}
import de.htwg.se.uno.model.gameComponent.base.state.GamePhase
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.model.gameComponent.{GameStateInterface, InputResult, Success}

case class GameStateMock( override val players: List[PlayerHand] = List.fill(2)(PlayerHand(List())),
                          override val currentPlayerIndex: Int = 0,
                          override val allCards: List[Card] = List.empty,
                          override val isReversed: Boolean = false,
                          override val discardPile: List[Card] = List.empty,
                          override val drawPile: List[Card] = List.empty,
                          override val selectedColor: Option[String] = None,
                          override val currentPhase: Option[GamePhase]
                        ) extends GameStateInterface(players: List[PlayerHand], currentPlayerIndex: Int,
                                        allCards: List[Card], isReversed: Boolean,
                                        discardPile: List[Card], drawPile: List[Card],
                                        selectedColor: Option[String],
                                        currentPhase: Option[GamePhase]
) {

  override def nextPlayer(): GameStateMock = {
    this.copy(currentPlayerIndex = (currentPlayerIndex + 1) % players.length)
  }

  override def checkForWinner(): Option[Int] = None

  override def setGameOver(): GameStateInterface = {
    this.setGameOver()
  }

  override def playCard(card: Card, chosenColor: Option[String] = None): GameStateMock = {
    this.copy(discardPile = card :: discardPile)
  }

  override def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String]): Boolean = true

  override def drawCardAndReturnDrawn(): (GameStateMock, Card) = {
    val dummyCard = WildCard("wild")
    (this.copy(), dummyCard)
  }

  override def inputHandler(input: String, gameBoard: ControllerInterface): InputResult = {
    Success(this)
  }
7
  override def notifyObservers(): Unit = {
    this.notifyObservers()
  }

  override def dealInitialCards(cardsPerPlayer: Int): GameStateInterface = {
    this.dealInitialCards(cardsPerPlayer)
  }

  override def playerSaysUno(playerIndex: Int): GameStateInterface = {
    this.playerSaysUno(playerIndex)
  }

  override def drawCard(playerHand: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
  (Card, PlayerHand, List[Card], List[Card]) = {
    this.drawCard(playerHand, drawPile, discardPile)
  }

  override def handleDrawCards(count: Int): GameStateInterface = {
    this.handleDrawCards(count)
  }

  override def copyWithPiles(drawPile: List[Card], discardPile: List[Card]): GameStateInterface = {
    this.copyWithPiles(drawPile, discardPile)
  }

  override def setSelectedColor(color: String): GameStateInterface = {
    this.setSelectedColor(color)
  }

  override def copyWithIsReversed(isReversed: Boolean): GameStateInterface = {
    this.copyWithIsReversed(isReversed)
  }

  override def copyWithSelectedColor(selectedColor: Option[String]): GameStateInterface = {
    this.copyWithSelectedColor(selectedColor)
  }

  override def copyWithPlayersAndPiles(players: List[PlayerHand], drawPile: List[Card],
                              discardPile: List[Card]): GameStateInterface = {
    this.copyWithPlayersAndPiles(players, drawPile, discardPile)
  }
}
