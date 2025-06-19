package de.htwg.se.uno.model.cardComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import play.api.libs.json.Json

class CardJsonFormatSpec extends AnyWordSpec {

  import CardJsonFormat.*

  "A CardJsonFormat" should {

    "serialize and deserialize a NumberCard correctly" in {
      val card: Card = NumberCard("red", 5)
      val json = Json.toJson(card)
      val expectedJson = Json.parse("""{
        "color": "red",
        "number": 5,
        "type": "NumberCard"
      }""")

      json shouldBe expectedJson
      val parsed = json.validate[Card]
      parsed.isSuccess shouldBe true
      parsed.get shouldBe card
    }

    "serialize and deserialize an ActionCard correctly" in {
      val card: Card = ActionCard("blue", "skip")
      val json = Json.toJson(card)
      val expectedJson = Json.parse("""{
        "color": "blue",
        "action": "skip",
        "type": "ActionCard"
      }""")

      json shouldBe expectedJson
      val parsed = json.validate[Card]
      parsed.isSuccess shouldBe true
      parsed.get shouldBe card
    }

    "serialize and deserialize a WildCard correctly" in {
      val card: Card = WildCard("wild")
      val json = Json.toJson(card)
      val expectedJson = Json.parse("""{
        "action": "wild",
        "type": "WildCard"
      }""")

      json shouldBe expectedJson
      val parsed = json.validate[Card]
      parsed.isSuccess shouldBe true
      parsed.get shouldBe card
    }

    "fail to deserialize an unknown card type" in {
      val unknownJson = Json.parse("""{
        "type": "FakeCard",
        "color": "red"
      }""")

      val result = unknownJson.validate[Card]
      result.isError shouldBe true
    }
  }
}
