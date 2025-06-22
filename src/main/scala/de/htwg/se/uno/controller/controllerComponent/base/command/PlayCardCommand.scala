package de.htwg.se.uno.controller.controllerComponent.base.command

import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, WildCard}
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.util.Command

case class PlayCardCommand(card: Card, chooseColor: Option[String] = None, gameBoard: ControllerInterface) extends Command {

  private var validPlay: Boolean = false
  private var previousState: Option[GameStateInterface] = None

  override def execute(): Unit = {
    gameBoard.gameState.foreach { state =>
      previousState = Some(state)
      val color = chooseColor.getOrElse("")

      if (state.isValidPlay(card, state.discardPile.headOption, chooseColor.orElse(state.selectedColor))) {
        validPlay = true

        val newState = state.playCard(card)

        val transitionedState = card match {
          case ActionCard(_, "skip") =>
            newState.nextPlayer().nextPlayer()

          case ActionCard(_, "reverse") =>
            newState.copyWithIsReversed(isReversed = !newState.isReversed).nextPlayer()

          case ActionCard(_, "draw two") =>
            newState.handleDrawCards(2).nextPlayer().nextPlayer()

          case WildCard("wild draw four") =>
            newState.copyWithSelectedColor(selectedColor = chooseColor).handleDrawCards(4).nextPlayer()

          case WildCard(_) =>
            newState.copyWithSelectedColor(selectedColor = chooseColor).nextPlayer()

          case _ =>
            newState.nextPlayer()
        }

        // debugging
        //      println(s"Before update: currentPlayerIndex = ${newState.currentPlayerIndex}")
        //      println(s"After transition: currentPlayerIndex = ${transitionedState.currentPlayerIndex}")

        gameBoard.updateState(transitionedState)
        transitionedState.notifyObservers()
      } else {
        validPlay = false
        println("Invalid play. Try again")
      }
    }
  }

  override def undo(): Unit = {
    previousState.foreach { oldState =>
      gameBoard.updateState(oldState)
    }
  }

  override def redo(): Unit = execute()
}
