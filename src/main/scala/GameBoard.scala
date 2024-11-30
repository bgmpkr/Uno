import scala.util.Random


case class GameBoard(drawPile: List[Card], discardPile: List[Card]) {

  def createDeckWithAllCards(): List[Card] = {
    val numberCards = for {
      // 1x0 and 2x1-9 for each color
      number <- 0 to 9
      color <- List("yellow", "red", "blue", "green")
    } yield {
      if (number == 0) {
        List(NumberCard(color, number))
      } else {
        List(
          NumberCard(color, number), //1. card
          NumberCard(color, number) //2. card
        )
      }
    }
    val allNumberCards = numberCards.flatten.toList

    val actionCards = List("draw two", "skip", "reverse").flatMap { action =>
      List(
        ActionCard("red", action),
        ActionCard("blue", action),
        ActionCard("green", action),
        ActionCard("yellow", action)
      )
    }
    val wildCards = List.fill(4)(WildCard("wild")) ++ List.fill(4)(WildCard("wild draw four"))
    allNumberCards ++ actionCards ++ wildCards
  }

  def shuffleDeck(): GameBoard = {
    val allCards = createDeckWithAllCards()
    val shuffledCards = Random.shuffle(allCards)
    GameBoard(shuffledCards, List.empty[Card])
  }

  def drawCard(playerHand: PlayerHand): (Card, PlayerHand, GameBoard) = {
    if (drawPile.isEmpty) {
      throw new RuntimeException("No cards left in the draw pile")
    }
    val drawnCard = drawPile.head
    val updatedDrawPile = drawPile.tail
    val updatedPlayerHand = playerHand.addCard(drawnCard)
    (drawnCard, updatedPlayerHand, copy(drawPile = updatedDrawPile))
  }

  def isValidPlay(card: Card, topCard: Option[Card]): Boolean = {
    topCard match {
      case None => true
      case Some(tCard) =>
        (card, tCard) match {

          case (WildCard("wild draw four"), WildCard("wild draw four")) => false
          case (WildCard(_), _) => true
          case (_, WildCard(_)) => true
          case (ActionCard(color, "draw two"), ActionCard(topColor, "draw two")) => color == topColor

          case (NumberCard(color, number), NumberCard(topColor, topNumber)) =>
            color == topColor || number == topNumber

          case (NumberCard(color, _), ActionCard(topColor, _)) => color == topColor
          case (ActionCard(color, _), NumberCard(topColor, _)) => color == topColor

          case (ActionCard(color, action), ActionCard(topColor, topAction)) =>
            color == topColor || action == topAction

          case _ => false
        }
    }
  }

  def playCard(card: Card, gameState: GameState): GameState = {
    val currentPlayerIndex = gameState.currentPlayerIndex
    val currentPlayerHand = gameState.players(currentPlayerIndex)
    val topCard = gameState.gameBoard.discardPile.lastOption
     
    if (!isValidPlay(card, topCard)) {
      var updatedPlayerHand = currentPlayerHand
      var drawPile = gameState.gameBoard.drawPile
      var playableCardFound = false
      var drawCount = 0

      while (!playableCardFound && drawPile.nonEmpty) {
        val (drawnCard, newHand, newGameBoard) = gameState.gameBoard.drawCard(updatedPlayerHand)
        updatedPlayerHand = newHand
        drawPile = newGameBoard.drawPile
        playableCardFound = isValidPlay(updatedPlayerHand.cards.last, topCard)
      }
      if (!playableCardFound) {
        return gameState.nextPlayer(gameState.copy(players =
          gameState.players.updated(currentPlayerIndex, updatedPlayerHand)))
      }
    }

    val updatedHand = currentPlayerHand.removeCard(card)

    val updatedDiscardPile = gameState.gameBoard.discardPile :+ card
    val updatedGameBoard = gameState.gameBoard.copy(discardPile = updatedDiscardPile)

    val baseGameState = gameState.copy(
      players = gameState.players.updated(currentPlayerIndex, updatedHand),
      gameBoard = updatedGameBoard
    )

    //---------------------------------------------- special card ------------------------------------------------------
    val finalGameState = card match {
      //------------ skip ------------
      case ActionCard(_, "skip") =>
        val firstSkipState = baseGameState.nextPlayer(baseGameState)
        val secondSkipState = firstSkipState.nextPlayer(firstSkipState)

        secondSkipState
      //------------ reverse ------------
      case  ActionCard(_, "reverse") =>
        val newGameState = baseGameState.copy(isReversed = !baseGameState.isReversed)
        val nextAfterReverse = newGameState.nextPlayer(newGameState)

        nextAfterReverse

      //------------ draw two ------------
      case ActionCard(_, "draw two") =>
        val nextPlayerIndex = baseGameState.nextPlayer(baseGameState).currentPlayerIndex

        val (updatedNextPlayerHand, updatedGameBoard) = (1 to 2).foldLeft((baseGameState.players(nextPlayerIndex),
              baseGameState.gameBoard)) {
          case ((hand, gameBoard), _) =>
            val (drawnCard, updatedHand, updatedGameBoard) = gameBoard.drawCard(hand)
            (updatedHand, updatedGameBoard)
        }

        val updatedGameState = baseGameState.copy(
          players = baseGameState.players.updated(nextPlayerIndex, updatedNextPlayerHand),
          gameBoard = updatedGameBoard,
          currentPlayerIndex = nextPlayerIndex
        )
        updatedGameState

      //------------ wild draw four ------------
      case WildCard("wild draw four") =>
        val nextPlayerIndex = baseGameState.nextPlayer(baseGameState).currentPlayerIndex

        val (updatedNextPlayerHand, updatedGameBoard) = (1 to 4).foldLeft((baseGameState.players(nextPlayerIndex), baseGameState.gameBoard)) {
          case ((hand, gameBoard), _) =>
            val (drawnCard, updatedHand, updatedGameBoard) = gameBoard.drawCard(hand)
            (updatedHand, updatedGameBoard)
        }

        val updatedGameState = baseGameState.copy(
          players = baseGameState.players.updated(nextPlayerIndex, updatedNextPlayerHand),
          gameBoard = updatedGameBoard,
          currentPlayerIndex = nextPlayerIndex
        )
        updatedGameState

      //------------ default ------------
      case _ => baseGameState.nextPlayer(baseGameState)
    }
    finalGameState
  }
}
