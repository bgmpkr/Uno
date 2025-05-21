import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.uno.controller.command.DrawCardCommand
import de.htwg.se.uno.controller.GameBoard
import de.htwg.se.uno.model.{Card, GameState, PlayerHand, NumberCard}

class DrawCardCommandSpec extends AnyWordSpec {

  "DrawCardCommand" should {

    "undo the draw by restoring the previous game state" in {
      val initialCard = NumberCard("red", 5)
      val playerHand = PlayerHand(List())
      val initialState = GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = List(initialCard),
        isReversed = false,
        discardPile = List(),
        drawPile = List(initialCard),
        selectedColor = None
      )

      GameBoard.updateState(initialState)

      val command = DrawCardCommand()

      // execute draw
      command.execute()
      val afterDrawState = GameBoard.gameState.get

      // undo draw - should revert to initial state
      command.undo()
      val afterUndoState = GameBoard.gameState.get

      assert(afterUndoState == initialState)
      assert(afterDrawState != initialState) // state changed after draw
    }

    "redo the draw by executing again" in {
      val initialCard = NumberCard("red", 5)
      val playerHand = PlayerHand(List())
      val initialState = GameState(
        players = List(playerHand),
        currentPlayerIndex = 0,
        allCards = List(initialCard),
        isReversed = false,
        discardPile = List(),
        drawPile = List(initialCard),
        selectedColor = None
      )

      GameBoard.updateState(initialState)

      val command = DrawCardCommand()

      // execute draw
      command.execute()
      val afterDrawState = GameBoard.gameState.get

      // undo draw
      command.undo()
      val afterUndoState = GameBoard.gameState.get

      // redo draw
      command.redo()
      val afterRedoState = GameBoard.gameState.get

      assert(afterRedoState == afterDrawState)
      assert(afterRedoState != afterUndoState)
    }
  }
}
