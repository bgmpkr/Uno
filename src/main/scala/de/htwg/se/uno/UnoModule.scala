package de.htwg.se.uno

import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.google.inject.name.Names
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import net.codingwell.scalaguice.ScalaModule
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.controller.controllerComponent.base.{GameBoard, GameBoardDI}
import de.htwg.se.uno.model.cardComponent.{Card, CardFactory, CardFactoryImpl}
import de.htwg.se.uno.model.gameComponent.base.state.*
import de.htwg.se.uno.model.playerComponent.PlayerHand

class UnoModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bindConstant().annotatedWith(Names.named("DefaultPlayers")).to(2)
    bindConstant().annotatedWith(Names.named("CardsPerPlayer")).to(7)

    @Provides
    def provideGameState(): GameStateInterface = {
      GameState(
        players = List.empty,
        allCards = List.empty,
        currentPlayerIndex = 0,
        isReversed = false,
        discardPile = List.empty,
        drawPile = List.empty,
        selectedColor = None,
        currentPhase = None
      )
    }
    bind[ControllerInterface].to[GameBoardDI]

    bind[CheckWinnerPhaseI].to[CheckWinnerPhase]
    bind[ColorWishPhaseI].to[ColorWishPhase]
    bind[DrawCardPhaseI].to[DrawCardPhase]
    bind[GameOverPhaseI].to[GameOverPhase]
    bind[PlayCardPhaseI].to[PlayCardPhase]
    bind[PlayerTurnPhaseI].to[PlayerTurnPhase]
    bind[ReversePhaseI].to[ReversePhase]
    bind[SkipPhaseI].to[SkipPhase]
    bind[StartPhaseI].to[StartPhase]
    bind[UnoCalledPhaseI].to[UnoCalledPhase]

    bind(classOf[CardFactory]).to(classOf[CardFactoryImpl])
    bind(new TypeLiteral[List[Card]]() {}).toInstance(List.empty)
    bind(new TypeLiteral[List[PlayerHand]]() {}).toInstance(List.empty)
    bind(new TypeLiteral[Option[GamePhase]]() {}).toInstance(None)
    bind(new TypeLiteral[Option[String]]() {}).toInstance(None)
    bind(classOf[UnoPhases])
  }
}
