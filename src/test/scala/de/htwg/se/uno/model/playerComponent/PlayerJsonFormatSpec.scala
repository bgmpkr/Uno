package de.htwg.se.uno.model.playerComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import play.api.libs.json.Json
import de.htwg.se.uno.model.cardComponent.*
import de.htwg.se.uno.model.playerComponent.PlayerJsonFormat.*

class PlayerJsonFormatSpec extends AnyWordSpec {

  "A PlayerJsonFormat" should {

    "serialize and deserialize a PlayerHand with multiple cards correctly" in {
      val playerHand = PlayerHand(
        cards = List(
          NumberCard("red", 3),
          ActionCard("blue", "draw two"),
          WildCard("wild")
        ),
        hasSaidUno = true
      )

      val json = Json.toJson(playerHand)
      val parsed = json.validate[PlayerHand]

      parsed.isSuccess shouldBe true
      parsed.get shouldBe playerHand
    }

    "serialize and deserialize an empty PlayerHand correctly" in {
      val playerHand = PlayerHand(
        cards = List.empty,
        hasSaidUno = false
      )

      val json = Json.toJson(playerHand)
      val parsed = json.validate[PlayerHand]

      parsed.isSuccess shouldBe true
      parsed.get shouldBe playerHand
    }

    "produce readable JSON structure with expected keys" in {
      val playerHand = PlayerHand(
        cards = List(NumberCard("yellow", 1)),
        hasSaidUno = false
      )

      val json = Json.toJson(playerHand)
      (json \ "cards").isDefined shouldBe true
      (json \ "hasSaidUno").isDefined shouldBe true
    }
  }
}
