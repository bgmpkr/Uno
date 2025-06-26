package de.htwg.se.uno.model.gameComponent

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.base.phase.GamePhase
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.Observable

trait GameStateInterface (
       val players: List[PlayerHand], val currentPlayerIndex: Int,
       val allCards: List[Card], val isReversed: Boolean = false,
       val discardPile: List[Card], val drawPile: List[Card],
       val selectedColor: Option[String] = None,
       val currentPhase: Option[GamePhase] = None
  ) extends Observable {
  def setGameOver(): GameStateInterface
  def copyWithPiles(drawPile: List[Card], discardPile: List[Card]): GameStateInterface
  def nextPlayer(): GameStateInterface
  def dealInitialCards(cardsPerPlayer: Int): GameStateInterface
  def checkForWinner(): Option[Int]
  def playerSaysUno(playerIndex: Int): GameStateInterface
  def drawCard(playerHand: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
  (Option[Card], PlayerHand, List[Card], List[Card])
  def playCard(card: Card, chosenColor: Option[String] = None): GameStateInterface
  def handleDrawCards(count: Int): GameStateInterface
  def drawTwoChainEnded(currentCard: Card, hand: List[Card]): Boolean
  def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String] = None): Boolean
  def drawCardAndReturnDrawn(): (GameStateInterface, Option[Card])
  def setSelectedColor(color: String): GameStateInterface
  def inputHandler(input: String, gameBoard: ControllerInterface): InputResult
  def copyWithIsReversed(isReversed: Boolean): GameStateInterface
  def copyWithSelectedColor(selectedColor: Option[String]): GameStateInterface
  def copyWithPlayersAndPiles(players: List[PlayerHand], drawPile: List[Card],
                              discardPile: List[Card]): GameStateInterface
}
