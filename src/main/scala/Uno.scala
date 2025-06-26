import com.google.inject.{Guice, Injector}
import de.htwg.se.uno.UnoModule
import de.htwg.se.uno.aview.UnoTUI
import de.htwg.se.uno.aview.gui.UnoGUI
import de.htwg.se.uno.aview.UnoGame
import de.htwg.se.uno.controller.controllerComponent.ControllerInterface
import de.htwg.se.uno.controller.controllerComponent.base.Controller
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Uno {
  val injector: Injector = Guice.createInjector(new UnoModule)

  val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])

  def main(args: Array[String]): Unit = {

    val gui =  new UnoGUI(controller)
    Controller.addObserver(gui)

    Future {
      gui.main(args)
    }

    while (Controller.gameState.isFailure) {
      Thread.sleep(100)
    }

    val tui = new UnoTUI(controller)
    Controller.addObserver(tui)

    UnoGame.inputLoop(tui)
  }
}