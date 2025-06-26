package de.htwg.se.uno

import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.google.inject.name.Names
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import net.codingwell.scalaguice.ScalaModule
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.controller.controllerComponent.base.ControllerDI
import de.htwg.se.uno.model.cardComponent.{Card, CardFactoryInterface, CardFactory}
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.fileIOComponent.fileIOJSON.FileIOJson
import de.htwg.se.uno.model.fileIOComponent.fileIOXML.FileIOXml
import de.htwg.se.uno.model.gameComponent.base.phase.*
import de.htwg.se.uno.model.playerComponent.PlayerHand

class UnoModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bindConstant().annotatedWith(Names.named("DefaultPlayers")).to(2)
    bindConstant().annotatedWith(Names.named("CardsPerPlayer")).to(7)

    @Provides
    def provideGameState(): GameStateInterface = {
      val cardFactory = new CardFactory
      val fullDeck = cardFactory.createFullDeck()
      val shuffleDeck = scala.util.Random.shuffle(fullDeck)

      val cardsPerPlayer = 7
      val numPlayers = 2
      val totalCardsNeeded = cardsPerPlayer * numPlayers + 1 // +1 für die erste Karte im Ablagestapel

      if (shuffleDeck.size < totalCardsNeeded) {
        throw new IllegalStateException(s"Not enough cards in deck: ${shuffleDeck.size} < $totalCardsNeeded")
      }

      val (player1Cards, rest1) = shuffleDeck.splitAt(cardsPerPlayer)
      val (player2Cards, rest2) = rest1.splitAt(cardsPerPlayer)
      val (topDiscard, rest3) = rest2.splitAt(1)

      GameState(
        players = List(PlayerHand(cards = player1Cards, hasSaidUno = false),
          PlayerHand(cards = player2Cards, hasSaidUno = false)),
        allCards = shuffleDeck,
        currentPlayerIndex = 0,
        isReversed = false,
        discardPile = topDiscard,
        drawPile = rest3,
        selectedColor = None,
        currentPhase = None
      )
    }
    bind[ControllerInterface].to[ControllerDI]
    bind(classOf[FileIOInterface]).to(classOf[FileIOJson])
    //bind(classOf[FileIOInterface]).to(classOf[FileIOXml])

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

    bind(classOf[CardFactoryInterface]).to(classOf[CardFactory])
    bind(new TypeLiteral[List[Card]]() {}).toInstance(List.empty)
    bind(new TypeLiteral[List[PlayerHand]]() {}).toInstance(List.empty)
    bind(new TypeLiteral[Option[GamePhase]]() {}).toInstance(None)
    bind(new TypeLiteral[Option[String]]() {}).toInstance(None)
    bind(classOf[UnoPhases])
  }
}
