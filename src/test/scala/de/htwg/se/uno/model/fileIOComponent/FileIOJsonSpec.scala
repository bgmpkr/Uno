package de.htwg.se.uno.model.fileIOComponent

import de.htwg.se.uno.model.cardComponent.{ActionCard, NumberCard}
import de.htwg.se.uno.model.fileIOComponent.fileIOJSON.FileIOJson
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.playerComponent.PlayerHand
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

import java.io.File

class FileIOJsonSpec extends AnyWordSpec {
  "A FileIOJson" when {
    "saving and loading a game" should {
      val fileIO = new FileIOJson
      val testFile = "testUno.json"
      val playerHands = List(
        PlayerHand(List(NumberCard("green", 4), ActionCard("red", "skip"))),
        PlayerHand(List(NumberCard("yellow", 8), ActionCard("blue", "reverse")))
      )
      val allCards = List.fill(5)(NumberCard("red", 8))
      val discardPile = List(NumberCard("green", 4))
      val drawPile = allCards.drop(1)

      val initialState = new GameState(
        players = playerHands,
        currentPlayerIndex = 0,
        allCards = allCards,
        isReversed = false,
        discardPile = discardPile,
        drawPile = drawPile,
        selectedColor = None,
        currentPhase = None
      )

      "correctly save the game state to a JSON file" in {
        fileIO.save(initialState, testFile)

        val savedFile = new File(fileIO.savedir + testFile)
        savedFile.exists() shouldBe true
        savedFile.length() should be > 0L
      }

      "correctly load the game state from a JSON file" in {
        fileIO.save(initialState, testFile)
        val loadedState = fileIO.load(testFile)

        loadedState shouldBe a[GameStateInterface]
        val loadedGameState = loadedState.asInstanceOf[GameState]

        loadedGameState.players should have size initialState.players.size
        loadedGameState.allCards should have size initialState.allCards.size
        loadedGameState.discardPile should have size initialState.discardPile.size
        loadedGameState.drawPile should have size initialState.drawPile.size

        new File(fileIO.savedir + testFile).delete()
      }
    }
  }  
}