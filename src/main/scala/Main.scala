import model.*
import scala.io.StdIn.readLine

object Main {
  def main(args: Array[String]): Unit = {
    println("Welcome to UNO!")

    val numberPlayers = readValidInt("How many players? (2-10): ", min = 2, max = 10)
    val cardsPerPlayer =  7

    // Initialize empty GameBoard, PlayerHands, and GameState
    val initialGameBoard = GameBoard(List.empty[Card], List.empty[Card]).shuffleDeck()

    val playerHands = List.fill(numberPlayers)(PlayerHand(List.empty[Card]))
    var gameState = GameState(playerHands, initialGameBoard, 0, initialGameBoard.drawPile)

    gameState = gameState.dealInitialCards(cardsPerPlayer)
    println("Let's start the Game!")
    Thread.sleep(2000)
    UnoTui.startGame(gameState)
  }

  def readValidInt(prompt: String, min: Int, max: Int): Int = {
    var valid = false
    var number = 0
    while (!valid) {
      print(prompt)
      val input = readLine()
      try {
        val parsed = input.toInt
        if (parsed >= min && parsed <= max) {
          number = parsed
          valid = true
        } else {
          println(s"Please enter a number between $min and $max.")
        }
      } catch {
        case _: NumberFormatException =>
          println("Invalid input. Please enter a number.")
      }
    }
    number
  }
}
