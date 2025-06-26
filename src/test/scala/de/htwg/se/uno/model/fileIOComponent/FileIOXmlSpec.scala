package de.htwg.se.uno.model.fileIOComponent.fileIOXML

import de.htwg.se.uno.controller.controllerComponent.base.Controller.fileIO
import de.htwg.se.uno.model.cardComponent.*
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.phase.GamePhase
import de.htwg.se.uno.model.playerComponent.PlayerHand
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

import java.io.File

class FileIOXmlSpec extends AnyWordSpec {
  "A FileIOXml" when {
    val fileIO = new FileIOXml
    val testFile = "testUno.xml"
    val player1 = PlayerHand(List(NumberCard("red", 5), ActionCard("blue", "draw two")), hasSaidUno = true)
    val player2 = PlayerHand(List(WildCard("wild")), hasSaidUno = false)
    val playerHands = List(
      PlayerHand(List(NumberCard("red", 5), ActionCard("blue", "draw two")), hasSaidUno = true),
      PlayerHand(List(WildCard("wild")), hasSaidUno = false)
    )
    val allCards = List.fill(5)(NumberCard("red", 8))
    val discardPile = List(NumberCard("green", 4))
    val drawPile = List(ActionCard("blue", "draw two"), WildCard("wild"))

    val initialState = new GameState(
      players = playerHands,
      currentPlayerIndex = 0,
      allCards = allCards,
      isReversed = false,
      discardPile = discardPile,
      drawPile = drawPile,
      selectedColor = Some("red"),
      currentPhase = None
    )

    "saving and loading a game" should {
      "correctly save the game state to an XML file" in {
        fileIO.save(initialState, testFile)

        val savedFile = new File(fileIO.savedir + testFile)
        savedFile.exists() shouldBe true
        savedFile.length() should be > 0L
      }

      "correctly load the game state from an XML file" in {
        fileIO.save(initialState, testFile)
        val loadedState = fileIO.load(testFile).asInstanceOf[GameState]

        loadedState.players should have size 2
        loadedState.players.head.cards should contain theSameElementsAs player1.cards
        loadedState.players.head.hasSaidUno shouldBe true
        loadedState.players(1).cards should contain theSameElementsAs player2.cards
        loadedState.players(1).hasSaidUno shouldBe false

        loadedState.currentPlayerIndex shouldBe 0
        loadedState.isReversed shouldBe false
        loadedState.discardPile should contain theSameElementsAs List(NumberCard("green", 4))
        loadedState.drawPile should contain theSameElementsAs List(ActionCard("blue", "draw two"), WildCard("wild"))
        loadedState.selectedColor shouldBe Some("red")
        loadedState.currentPhase shouldBe None
      }

      "handle empty optional fields correctly" in {
        val stateWithoutOptionals = new GameState(
          players = List(player1),
          currentPlayerIndex = 0,
          allCards = List(NumberCard("red", 5)),
          isReversed = false,
          discardPile = List(NumberCard("red", 5)),
          drawPile = List.empty,
          selectedColor = None,
          currentPhase = None
        )

        fileIO.save(stateWithoutOptionals, testFile)
        val loadedState = fileIO.load(testFile).asInstanceOf[GameState]

        loadedState.selectedColor shouldBe None
        loadedState.currentPhase shouldBe None
        loadedState.drawPile shouldBe empty

        new File(fileIO.savedir + testFile).delete() shouldBe true
      }
    }
  }
}