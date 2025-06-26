package de.htwg.se.uno.model.gameComponent.base.phase

import play.api.libs.json.*

object GamePhaseJsonFormat {
  implicit val gamePhaseFormat: Format[GamePhase] = new Format[GamePhase] {
    override def reads(json: JsValue): JsResult[GamePhase] = json.validate[String].flatMap {
      case "DrawCardPhase" => JsSuccess(DrawCardPhase.asInstanceOf[GamePhase])
      case "PlayCardPhase" => JsSuccess(PlayCardPhase.asInstanceOf[GamePhase])
      case "GameOverPhase" => JsSuccess(GameOverPhase.asInstanceOf[GamePhase])
      case other => JsError(s"Unknown GamePhase: $other")
    }

    override def writes(phase: GamePhase): JsValue = JsString(phase.getClass.getSimpleName.stripSuffix("$"))
  }
}
