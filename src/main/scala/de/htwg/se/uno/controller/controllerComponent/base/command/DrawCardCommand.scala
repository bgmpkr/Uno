package de.htwg.se.uno.controller.controllerComponent.base.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.Card
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.Command

case class DrawCardCommand(gameBoard: ControllerInterface) extends Command {

  var drawnCard: Option[Card] = None
  private var previousState: Option[GameStateInterface] = None
  
  override def execute(): Unit = {
    gameBoard.gameState.foreach { state =>
      previousState = Some(state)
      val currentPlayer = state.players(state.currentPlayerIndex)
      val (cardDrawn, updatedPlayerHand, updatedDrawPile, updatedDiscardPile) =
        state.drawCard(
          playerHand = currentPlayer,
          drawPile = state.drawPile,
          discardPile = state.discardPile
        )

      drawnCard = Some(cardDrawn)

      val updatedPlayers = state.players.updated(
        state.currentPlayerIndex, updatedPlayerHand
      )

      val newGameState = state.copyWithPlayersAndPiles(updatedPlayers, updatedDrawPile, updatedDiscardPile)

      gameBoard.updateState(newGameState)
    }
  }  

  override def undo(): Unit = {
    previousState.foreach { oldState =>
      gameBoard.updateState(oldState)
      oldState.notifyObservers()
    }
  }

  override def redo(): Unit = execute()
}
