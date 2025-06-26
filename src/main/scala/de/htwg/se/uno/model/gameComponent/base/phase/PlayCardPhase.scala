package de.htwg.se.uno.model.gameComponent.base.phase

import com.google.inject.Inject
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState

case class PlayCardPhase @Inject() (context: UnoPhases) extends PlayCardPhaseI {

  override def playCard(card: Card): GamePhase = {
    context.gameState = context.gameState.playCard(card)
    context.setState(PlayerTurnPhase(context))

    val finalGameState = card match {
      case ActionCard(_, "skip") =>
        context.gameState = context.gameState.nextPlayer().nextPlayer()
        PlayerTurnPhase(context)

      case ActionCard(_, "reverse") =>
        context.gameState = context.gameState.copyWithIsReversed(isReversed = !context.gameState.isReversed)
        PlayerTurnPhase(context)

      case ActionCard(_, "draw two") =>
        context.gameState = handlePenaltyCards(context.gameState, 2).nextPlayer()
        PlayerTurnPhase(context)

      case WildCard("wild draw four") =>
        context.gameState = handlePenaltyCards(context.gameState, 4).nextPlayer()
        PlayerTurnPhase(context)

      case WildCard("wild") =>
        context.gameState = context.gameState.nextPlayer()
        PlayerTurnPhase(context)

      case _ =>
        context.gameState = context.gameState.nextPlayer()
        PlayerTurnPhase(context)
    }

    checkUnoStatus(context)
    finalGameState
  }

  private def handlePenaltyCards(state: GameStateInterface, count: Int): GameStateInterface = {
    val nextPlayerIdx = if (state.isReversed) {
      (state.currentPlayerIndex - 1 + state.players.length) % state.players.length
    } else {
      (state.currentPlayerIndex + 1) % state.players.length
    }

    val (updatedHand, updatedDrawPile, _) =
      (1 to count).foldLeft((state.players(nextPlayerIdx), state.drawPile, state.discardPile)) {
        case ((hand, draw, _), _) =>
          val (_, newHand, newDraw, _) = state.drawCard(hand, draw, Nil)
          (newHand, newDraw, Nil)
      }

    state.copyWithPlayersAndPiles(
      state.players.updated(nextPlayerIdx, updatedHand),
      updatedDrawPile,
      state.discardPile
    )
  }

  private def checkUnoStatus(context: UnoPhases): Unit = {
    val currentPlayer = context.gameState.currentPlayerIndex
    if (context.gameState.players(currentPlayer).cards.size == 1) {
      context.setState(UnoCalledPhase(context))
    }
  }

  override def nextPlayer(): GamePhase = PlayerTurnPhase(context)

  override def dealInitialCards(): GamePhase = {
    println("Cannot deal cards during PlayCardState")
    this
  }

  override def checkForWinner(): GamePhase = GameOverPhase()

  override def playerSaysUno(): GamePhase = UnoCalledPhase(context)

  override def drawCard(): GamePhase = {
    println("Cannot draw a card during PlayCardState")
    this
  }

  override def isValidPlay(card: Card): Boolean = {
    val topCard = context.gameState.discardPile.lastOption
    context.gameState.isValidPlay(card, topCard, context.gameState.selectedColor)
  }
}
