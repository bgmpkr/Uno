package de.htwg.se.uno.model.gameComponent.state

import de.htwg.se.uno.model.cardComponent.NumberCard
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.{GamePhase, UnoPhases}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.ByteArrayOutputStream

class UnoPhasesSpec extends AnyWordSpec with Matchers {

    class DummyState extends GamePhase {
        override def playCard(): GamePhase = this
        override def drawCard(): GamePhase = this
        override def nextPlayer(): GamePhase = this
        override def dealInitialCards(): GamePhase = this
        override def checkForWinner(): GamePhase = this
        override def playerSaysUno(): GamePhase = this
        override def isValidPlay: Boolean = true
    }

    "UnoStates" should {

        "delegate playCard call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            val dummyCard = NumberCard("green", 5)
            unoStates.setState(dummy)

            unoStates.playCard(dummyCard)
            unoStates.state shouldBe dummy
        }

        "delegate drawCard call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            unoStates.setState(dummy)

            unoStates.drawCard()
            unoStates.state shouldBe dummy
        }

        "delegate nextPlayer call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            unoStates.setState(dummy)

            unoStates.nextPlayer()
            unoStates.state shouldBe dummy
        }

        "delegate dealInitialCards call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            unoStates.setState(dummy)

            unoStates.dealInitialCards()
            unoStates.state shouldBe dummy
        }

        "delegate checkForWinner call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            unoStates.setState(dummy)

            unoStates.checkForWinner()
            unoStates.state shouldBe dummy
        }

        "delegate playerSaysUno call to currentState and update state" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)
            val dummy = new DummyState
            unoStates.setState(dummy)

            unoStates.playerSaysUno()
            unoStates.state shouldBe dummy
        }

        "tryPlayCard calls playCard if isValidPlay is true" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)

            val dummyCard = NumberCard("green", 5)
            var played = false
            val dummy = new DummyState {
                override def playCard(): GamePhase = {
                        played = true
                        this
                }
            }

            unoStates.setState(dummy)
            unoStates.tryPlayCard(dummyCard)
            played shouldBe true
        }

        "tryPlayCard prints 'Invalid play.' if isValidPlay is false" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)

            val dummyCard = NumberCard("green", 5)
            val dummy = new DummyState {
                override def isValidPlay: Boolean = false
            }
            unoStates.setState(dummy)

            import java.io.PrintStream

            val outStream = new ByteArrayOutputStream()
            Console.withOut(new PrintStream(outStream)) {
                unoStates.tryPlayCard(dummyCard)
            }

            outStream.toString should include ("Invalid play.")
        }

        "setSelectedColor updates selectedColor" in {
            val gameState = GameState(List(), 0, List(), false, List(), List())
            val unoStates = new UnoPhases(gameState)

            unoStates.selectedColor shouldBe None
            unoStates.setSelectedColor("red")
            unoStates.selectedColor shouldBe Some("red")
        }
    }
}
