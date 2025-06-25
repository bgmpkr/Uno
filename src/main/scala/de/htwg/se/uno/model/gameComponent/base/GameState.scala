package de.htwg.se.uno.model.gameComponent.base

import com.google.inject.Inject
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.command.PlayCardCommand
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.gameComponent.{Failure, InputResult, Success}
import de.htwg.se.uno.model.playerComponent.PlayerHand
import de.htwg.se.uno.model.playerComponent.PlayerJsonFormat.*
import de.htwg.se.uno.model.cardComponent.CardJsonFormat.*
import de.htwg.se.uno.model.gameComponent.base.state.GamePhaseJsonFormat.*
import de.htwg.se.uno.util.Observable
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.state.{GameOverPhase, GamePhase}

import scala.util.Try

case class GameState @Inject() (
      override val players: List[PlayerHand], override val currentPlayerIndex: Int,
      override val allCards: List[Card], override val isReversed: Boolean = false,
      override val discardPile: List[Card], override val drawPile: List[Card],
      override val selectedColor: Option[String] = None,
      override val currentPhase: Option[GamePhase] = None
  ) extends Observable, GameStateInterface(players: List[PlayerHand], currentPlayerIndex: Int,
                              allCards: List[Card], isReversed: Boolean,
                              discardPile: List[Card], drawPile: List[Card],
                              selectedColor: Option[String],
                              currentPhase: Option[GamePhase]
  ){

  def nextPlayer(): GameStateInterface = {
    val playerCount = players.length
    val nextIndex = if (isReversed) {
      (currentPlayerIndex - 1 + playerCount) % playerCount
    } else {
      (currentPlayerIndex + 1) % playerCount
    }
    println(s"Next Player Index: $nextIndex")
    this.copy(currentPlayerIndex = nextIndex)
  }

  def dealInitialCards(cardsPerPlayer: Int): GameStateInterface = {
    var updatedGameState: GameStateInterface = this
    for (cardNum <- 1 to cardsPerPlayer) {
      for (playerIndex <- updatedGameState.players.indices) {
        val (drawnCard, updatedHand, updatedDrawPile, updatedDiscardPile) =
          updatedGameState.drawCard(updatedGameState.players(playerIndex),
            updatedGameState.drawPile, updatedGameState.discardPile)

        drawnCard match {
          case Some(_) =>
            val updatedPlayers = updatedGameState.players.updated(playerIndex, updatedHand)

            updatedGameState = updatedGameState.copyWithPiles(updatedDrawPile, updatedDiscardPile)
              .asInstanceOf[GameState]

            updatedGameState = updatedGameState match {
              case gs: GameState => gs.copy(players = updatedPlayers)
              case other => other
            }

          case None =>
            println(s"⚠️ Could not deal card $cardNum to player $playerIndex - insufficient cards")
            return updatedGameState
        }
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

  def setGameOver(): GameStateInterface = {
    this.copy(currentPhase = Some(GameOverPhase()))
  }

  def playerSaysUno(playerIndex: Int): GameState = {
    val updatedPlayers = players.updated(
      playerIndex,
      players(playerIndex).copy(hasSaidUno = true))
    if (updatedPlayers(playerIndex).cards.isEmpty && updatedPlayers(playerIndex).hasSaidUno) {
      this.copy(
        players = updatedPlayers,
        currentPhase = Some(GameOverPhase())
      )
    } else {
      this.copy(players = updatedPlayers)
    }
  }

  def drawCard(playerHand: PlayerHand, drawPile: List[Card], discardPile: List[Card]):
  (Option[Card], PlayerHand, List[Card], List[Card]) = {

    if (drawPile.isEmpty && discardPile.size <= 1) {
      println("❌ No cards available to draw!")
      return (None, playerHand, drawPile, discardPile)
    }

    val reshuffledDrawPile =
      if (drawPile.isEmpty && discardPile.size > 1) {
        val shuffled = scala.util.Random.shuffle(discardPile.init)
        println(s"🔄 Reshuffling discard pile: ${shuffled.size} cards")
        shuffled
      } else {
        drawPile
      }

    val updatedDiscardPile =
      if (drawPile.isEmpty && discardPile.size > 1)
        List(discardPile.last)
      else
        discardPile

    if (reshuffledDrawPile.isEmpty) {
      println("❌ No cards available to draw after reshuffling!")
      return (None, playerHand, reshuffledDrawPile, updatedDiscardPile)
    }

    val drawnCard = reshuffledDrawPile.head
    val newDrawPile = reshuffledDrawPile.tail
    val updatedPlayerHand = playerHand + drawnCard

    (Some(drawnCard), updatedPlayerHand, newDrawPile, updatedDiscardPile)
  }


  def playCard(card: Card, chosenColor: Option[String] = None): GameStateInterface = {
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

  def handleDrawCards(count: Int): GameStateInterface = {
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

  override def isValidPlay(card: Card, topCard: Option[Card], selectedColor: Option[String] = None): Boolean = {

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

  def drawCardAndReturnDrawn(): (GameStateInterface, Option[Card]) = {
    val (maybeCard, updatedPlayer, newDrawPile, newDiscardPile) =
      drawCard(players(currentPlayerIndex), drawPile, discardPile)

    maybeCard match {
      case Some(card) =>
        val updatedPlayers = players.updated(currentPlayerIndex, updatedPlayer)
        val newState = this.copy(
          drawPile = newDrawPile,
          discardPile = newDiscardPile,
          players = updatedPlayers
        )
        println(s"✅ Player $currentPlayerIndex drew: $card")
        (newState, Some(card))

      case None =>
        println("⚠️ Card could not be drawn - no cards available.")
        (this, None)
    }
  }

  def setSelectedColor(color: String): GameStateInterface = {
    this.copy(selectedColor = Some(color))
  }

  def inputHandler(input: String, gameBoard: ControllerInterface): InputResult = {
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

      case s"play card:$index" =>
        Try(index.toInt) match {
          case scala.util.Success(idx) if idx >= 0 && idx < currentPlayer.cards.length =>
            val card = currentPlayer.cards(idx)
            val topCard = discardPile.lastOption
            val chooseColor = selectedColor

            val isValid = isValidPlay(card, topCard)
            val command = PlayCardCommand(card, chooseColor, gameBoard)

            gameBoard.executeCommand(command)

            if (!isValid) {
              println("⚠️ Wrong card played. Your play will be undone. A penalty card will be drawn.")

              gameBoard.undoCommand()

              gameBoard.gameState match {
                case scala.util.Success(state) =>
                  val (newState, drawnCard) = state.drawCardAndReturnDrawn()
                  gameBoard.updateState(newState)
                  Failure("Invalid play. You received a penalty card.")

                case scala.util.Failure(_) =>
                  Failure("Game state not initialized.")
              }
            } else {
              gameBoard.gameState match {
                case scala.util.Success(state) => Success(state)
                case scala.util.Failure(_) => Failure("Game state not initialized.")
              }
            }

          case scala.util.Success(_) =>
            Failure("Invalid card index.")
          case scala.util.Failure(_) =>
            Failure("Card index must be a digit.")
        }

    }
  }

  override def copyWithPiles(drawPile: List[Card], discardPile: List[Card]): GameStateInterface = {
    this.copy(
      drawPile = drawPile,
      discardPile = discardPile,
      allCards = discardPile ++ drawPile
    )
  }

  override def notifyObservers(): Unit = {
    super.notifyObservers()
  }

  override def copyWithIsReversed(isReversed: Boolean): GameStateInterface = {
    this.copy(isReversed = isReversed)
  }

  override def copyWithSelectedColor(selectedColor: Option[String]): GameStateInterface = {
    this.copy(selectedColor = selectedColor)
  }

  override def copyWithPlayersAndPiles(players: List[PlayerHand], drawPile: List[Card],
                               discardPile: List[Card]): GameStateInterface = {
    this.copy(
      players = players,
      drawPile = drawPile,
      discardPile = discardPile
    )
  }

  object GameState {
    import play.api.libs.json.*
    implicit val stateWrites: Writes[GameState] = Json.writes[GameState]
    implicit val stateReads: Reads[GameState] = Json.reads[GameState]
    implicit val gameStateFormat: OFormat[GameState] = Json.format[GameState]
  }
}