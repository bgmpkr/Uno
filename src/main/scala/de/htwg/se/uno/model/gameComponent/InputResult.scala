package de.htwg.se.uno.model.gameComponent

sealed trait InputResult

case class Success()(using game: GameStateInterface) extends InputResult

case class Failure(reason: String) extends InputResult

