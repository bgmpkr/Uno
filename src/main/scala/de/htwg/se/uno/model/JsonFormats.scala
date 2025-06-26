package de.htwg.se.uno.model

import de.htwg.se.uno.model.cardComponent.CardJsonFormat as CardFormats
import de.htwg.se.uno.model.playerComponent.PlayerJsonFormat as PlayerFormats
import de.htwg.se.uno.model.gameComponent.base.phase.GamePhaseJsonFormat as GamePhaseFormats
import de.htwg.se.uno.model.gameComponent.base.GameState
import play.api.libs.json.*

object JsonFormats {

  import CardFormats.*
  import PlayerFormats.*
  import GamePhaseFormats.*

  implicit val gameStateFormat: OFormat[GameState] = Json.format[GameState]
}
