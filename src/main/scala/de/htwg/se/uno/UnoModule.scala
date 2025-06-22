package de.htwg.se.uno

import com.google.inject.AbstractModule
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.controller.controllerComponent.base.GameBoardDI
import de.htwg.se.uno.model.gameComponent.base.state.*

class UnoModule extends AbstractModule {
  override def configure(): Unit = {
    given defaultPlayers: Int = 2
    given cardsPerPlayer: Int = 7

    given gameState: GameStateInterface = GameState(players = List.empty,
      currentPlayerIndex = 0,
      allCards = List.empty,
      isReversed = false,
      discardPile = List.empty,
      drawPile = List.empty,
      selectedColor = None,
      currentPhase = None)
    given controller: ControllerInterface = new GameBoardDI

    given unoPhases: UnoPhases = new UnoPhases()
    given checkWinnerPhase: CheckWinnerPhaseI = CheckWinnerPhase(unoPhases)
    given colorWishPhase: ColorWishPhaseI = ColorWishPhase(unoPhases)
    given drawCardPhase: DrawCardPhaseI = DrawCardPhase(unoPhases)
    given gameOverPhase: GameOverPhaseI = GameOverPhase()
    given playerTurnPhase: PlayerTurnPhaseI = PlayerTurnPhase(unoPhases)
    given reversePhase: ReversePhaseI = ReversePhase(unoPhases)
    given skipPhase: SkipPhaseI = SkipPhase(unoPhases)
    given startPhase: StartPhaseI = StartPhase(unoPhases)
    given unoCalledPhase: UnoCalledPhaseI = UnoCalledPhase(unoPhases)

  }
}
