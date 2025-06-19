import de.htwg.se.uno.aview.UnoTUI
import de.htwg.se.uno.aview.gui.UnoGUI
import de.htwg.se.uno.aview.UnoGame
import de.htwg.se.uno.controller.controllerComponent.base.GameBoard
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    val controller = GameBoard

    val gui =  new UnoGUI(controller)
    GameBoard.addObserver(gui)

    Future {
      gui.main(args)
    }

    while (GameBoard.gameState.isFailure) {
      Thread.sleep(100)
    }

    val tui = new UnoTUI(controller)
    GameBoard.addObserver(tui)

    UnoGame.inputLoop(tui)
  }
}