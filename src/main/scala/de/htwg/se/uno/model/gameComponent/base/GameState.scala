package de.htwg.se.uno.model.gameComponent.base

import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import de.htwg.se.uno.controller.controllerComponent.base.command.PlayCardCommand
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.{Failure, InputResult, Success}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.util.Observable
import de.htwg.se.uno.model.gameComponent.GameStateInterface

import scala.util.Try

case class GameState( players: List[PlayerHand], currentPlayerIndex: Int,
                      allCards: List[Card], isReversed: Boolean = false,
                      discardPile: List[Card], drawPile: List[Card], selectedColor: Option[String] = None)
  extends Observable, GameStateInterface {

  def nextPlayer(): GameState= {
    val playerCount = players.length
    val nextIndex = if (isReversed) {
      (currentPlayerIndex - 1 + playerCount) % playerCount
    } else {
      (currentPlayerIndex + 1) % playerCount
    }
    println(s"Next Player Index: $nextIndex")
    this.copy(currentPlayerIndex = nextIndex)
  }

  def dealInitialCards(cardsPerPlayer: Int): GameState = {
    var updatedGameState = this
    for (_ <- 1 to cardsPerPlayer) {
      for (playerIndex <- updatedGameState.players.indices) {
        val (drawnCard, updatedHand, updatedDrawPile, updatedDiscardPile) =
          updatedGameState.drawCard(updatedGameState.players(playerIndex),
            updatedGameState.drawPile, updatedGameState.discardPile)

        updatedGameState = updatedGameState.copy(
          players = updatedGameState.players.updated(playerIndex, updatedHand),
          drawPile = updatedDrawPile,
          discardPile = updatedDiscardPile
        )
      }
    }
    updatedGameState
  }

  def checkForWinner(): Option[Int] = {
    players.zipWithIndex.find { case (hand, _) =>
      hand.isEmpty
    } match {
      case Some((_, winnerIndex)) =>
        Some(winnerIndex)
      case None =>
        None
    }
  }

  def playerSaysUno(playerIndex: Int): GameState = {
    val updatedPlayers = players.updated(
      playerIndex,
      players(playerIndex).copy(hasSaidUno = true))
    this.copy(players = updatedPlayers)
  }

  def drawCard(playerHand: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
  (Card, PlayerHand, List[Card], List[Card]) = {
    if (drawPile.isEmpty) {
      if (discardPile.size <= 1) {
        throw new RuntimeException("No cards left in the draw pile")
      } else {
        val reshuffled = scala.util.Random.shuffle(discardPile.init)
        return drawCard(playerHand, reshuffled, List(discardPile.last))
      }
    }

    val drawnCard = drawPile.head
    val updatedDrawPile = drawPile.tail
    val updatedPlayerHand = playerHand + drawnCard
    (drawnCard, updatedPlayerHand, updatedDrawPile, discardPile)
  }

  def playCard(card: Card, chosenColor: Option[String] = None): GameState = {
    if (players.exists(_.cards.isEmpty)) return this

    val topCard = discardPile.headOption
    if (!isValidPlay(card, topCard)) {
      println("Invalid play.")
      return this
    }

    val newSelectedColor = card match {
      case WildCard(_) | WildCard("wild draw four") => chosenColor
      case _ => None
    }

    val updatedHand = players(currentPlayerIndex).removeCard(card)
    val updatedDiscardPile = card :: discardPile

    this.copy(
      players = players.updated(currentPlayerIndex, updatedHand),
      discardPile = updatedDiscardPile,
      selectedColor = if (card.isInstanceOf[WildCard]) selectedColor else None,
    )
  }

  def handleDrawCards(count: Int): GameState = {
    val nextPlayerIndex = if (isReversed) {
      (currentPlayerIndex - 1 + players.length) % players.length
    } else {
      (currentPlayerIndex + 1) % players.length
    }

    val (updatedHand, updatedDrawPile, _) =
      (1 to count).foldLeft((players(nextPlayerIndex), drawPile, discardPile)) {
        case ((hand, draw, _), _) =>
          val (_, newHand, newDraw, _) = drawCard(hand, draw, Nil)
          (newHand, newDraw, Nil)
      }

    this.copy(
      players = players.updated(nextPlayerIndex, updatedHand),
      drawPile = updatedDrawPile
    )
  }

  def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String] = None): Boolean = {

    selectedColor match {
      case Some(color) =>
        card match {
          case WildCard(_) => true
          case ActionCard(c, _) => c == color
          case NumberCard(c, v) => c == color
        }
      case None =>

        topCard match {
          case None => true

          case Some(tCard) =>
            (card, tCard) match {

              case (ActionCard(_, "draw two"), ActionCard(_, "draw two")) => true
              case (WildCard("wild draw four"), WildCard("wild draw four")) => false

              case (ActionCard(color, "draw two"), NumberCard(topColor, _)) =>
                color == topColor || selectedColor.contains(color)

              case (NumberCard(color, number), NumberCard(topColor, topNumber)) =>
                color == topColor || number == topNumber || selectedColor.contains(color)

              case (NumberCard(color, _), ActionCard(topColor, _)) =>
                color == topColor || selectedColor.contains(color)

              case (ActionCard(color, _), NumberCard(topColor, _)) =>
                color == topColor || selectedColor.contains(color)

              case (ActionCard(color, action), ActionCard(topColor, topAction)) =>
                color == topColor || action == topAction || selectedColor.contains(color)

              case (WildCard("wild"), _) => true
              case (WildCard("wild draw four"), _) => true
              case (_, WildCard("wild")) => true
              case (_, WildCard("wild draw four")) => true

              case _ => false
            }
        }
    }
  }

  def drawCardAndReturnDrawn(): (GameState, Card) = {
    val card = drawPile.head
    val updatedPlayer = players(currentPlayerIndex) + card
    val updatedPlayers = players.updated(currentPlayerIndex, updatedPlayer)
    val newState = this.copy(drawPile = drawPile.tail, players = updatedPlayers)
    (newState, card)
  }

  def setSelectedColor(color: String): GameState = {
    this.copy(selectedColor = Some(color))
  }

  def inputHandler(input: String): InputResult = {
    val currentPlayer = players(currentPlayerIndex)

    input match {
      case s"play wild:$index:$color" =>
        Try(index.toInt) match {
          case scala.util.Success(index) if index >= 0 && index < currentPlayer.cards.length =>
            currentPlayer.cards(index) match {
              case wild: WildCard =>
                val playedCard = WildCard(wild.action)
                val updatedGame =
                  setSelectedColor(color)
                  playCard(playedCard)
                Success(updatedGame)

              case _ =>
                Failure("Selected card is not a wild card.")
            }

          case scala.util.Success(_) =>
            Failure("Invalid card index.")

          case scala.util.Failure(_) =>
            Failure("Card index must be a number.")
        }

//      case s"play card:$index" =>
//        scala.util.Try(index.toInt) match {
//          case scala.util.Success(index) if index >= 0 && index < currentPlayer.cards.length =>
//            val card = currentPlayer.cards(index)
//            val topCard = discardPile.lastOption
//
//            if (!isValidPlay(card, topCard)) {
//              Failure("Invalid card! Please select a valid card.")
//            } else {
//              val updatedGame = playCard(card)
//              Success(updatedGame)
//            }
//
//          case scala.util.Success(_) =>
//            Failure("Invalid card index.")
//
//          case scala.util.Failure(_) =>
//            Failure("Card index must be a number.")
//        }
//
//      case _ =>
//        Failure("Unknown input command.")
//    }
      case s"play card:$index" =>
        Try(index.toInt) match {
          case scala.util.Success(idx) if idx >= 0 && idx < currentPlayer.cards.length =>
            val card = currentPlayer.cards(idx)
            val topCard = discardPile.lastOption
            val chooseColor = selectedColor
            val controller = GameBoard

            val isValid = isValidPlay(card, topCard)
            val command = PlayCardCommand(card, chooseColor, controller)

            controller.executeCommand(command)

            if (!isValid) {
              println("⚠️ Wrong card played. Your play will be undone. A penalty card will be drawn.")

              controller.undoCommand()

              val (newState, drawnCard) = controller.gameState.get.drawCardAndReturnDrawn()
              controller.updateState(newState)

              return Failure("Invalid play. You received a penalty card.")
            }

            Success(controller.gameState.get)

          case scala.util.Success(_) =>
            Failure("Invalid card index.")
          case scala.util.Failure(_) =>
            Failure("Card index must be a digit.")
        }

    }
  }

  override def notifyObservers(): Unit = {
    super.notifyObservers()
  }
}