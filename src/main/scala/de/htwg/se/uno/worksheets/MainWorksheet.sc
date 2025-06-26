import de.htwg.se.uno.model.cardComponent.{ActionCard, NumberCard, WildCard}

// testing the card class
val numCard = NumberCard("red", 5)
val actionCard = ActionCard("blue", "draw two")
val wildCard = WildCard("wild draw four")

numCard.color      // expected: "red"
numCard.number     // expected: 5
actionCard.action  // expected: "draw two"
wildCard.color     // expected: "wild"

//######################################################################################################################
//----------------------- creating a random game scenario for main and test somgit e methods -------------------------------
/** Note:
 * The test is random, as cards are shuffled and dealt each time.
 * Players simply play the first card in their hand.
 * If the test takes too long, try running it again due to the randomness of card distribution.
 */

// Game with two Players, initialize GameBoard and -State
val initialGameBoard = Controller(List.empty[Card], List.empty[Card]).shuffleDeck()
val players = List(PlayerHand(List.empty[Card]), PlayerHand(List.empty[Card]))
var gameState = GameState(players, initialGameBoard, 0, initialGameBoard.drawPile)

// Deal initial cards
gameState = gameState.dealInitialCards(2)

// Show PlayerHands
println("After dealing cards:")
gameState.players.zipWithIndex.foreach { case (hand, index) =>
  println(s"Player ${index + 1}:")
  hand.displayHand()
  println(s"'Uno'? ${hand.hasUno}")
  println()
}

println("\n--- round 1 ---")

var currentPlayerIdx = gameState.currentPlayerIndex
var currentPlayer = gameState.players(currentPlayerIdx)

// Current Player plays
val cardToPlay = currentPlayer.cards.head
gameState = gameState.gameBoard.playCard(cardToPlay, gameState)  // Player plays card

println(s"Player ${currentPlayerIdx + 1} has discarded the card $cardToPlay.")
println(s"Player ${currentPlayerIdx + 1} now has ${gameState.players(currentPlayerIdx).cards.length} cards.")
println(s"Can Player ${currentPlayerIdx + 1} say 'Uno'? ${gameState.players.head.hasUno}")

// Player says "Uno"
if (currentPlayer.hasUno) {
  gameState = gameState.copy(players = gameState.players.updated(currentPlayerIdx, currentPlayer.sayUno()))
  println(s"Player ${currentPlayerIdx + 1} says 'Uno'!")
}

// Next Player plays
println("\n--- Next Player plays ---")
// now move to the next player
currentPlayerIdx = gameState.currentPlayerIndex
currentPlayer = gameState.players(currentPlayerIdx)

// Next Player plays
val cardToPlayNext = currentPlayer.cards.head
gameState = gameState.gameBoard.playCard(cardToPlayNext, gameState)

println(s"Player ${currentPlayerIdx + 1} has discarded the card $cardToPlayNext.")
println(s"Player ${currentPlayerIdx + 1} now has ${gameState.players(currentPlayerIdx).cards.length} cards.")
println(s"Can Player ${currentPlayerIdx + 1} say 'Uno'? ${gameState.players(currentPlayerIdx).hasUno}")

// Next Player says "Uno"
if (currentPlayer.hasUno) {
  gameState = gameState.copy(players = gameState.players.updated(currentPlayerIdx, currentPlayer.sayUno()))
  println(s"Player ${currentPlayerIdx + 1} says 'Uno'!")
}

// check for a winner
println("\n--- check for a winner ---")
gameState.checkForWinner()

