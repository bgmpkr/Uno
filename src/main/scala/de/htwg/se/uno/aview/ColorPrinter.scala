package de.htwg.se.uno.aview

import de.htwg.se.uno.model.*
import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}

object ColorPrinter {
  private val Reset = "\u001B[0m"
  private val Red = "\u001B[31m"
  private val Green = "\u001B[32m"
  private val Blue = "\u001B[34m"
  private val Yellow = "\u001B[93m"

  def printCard(card: Card): Unit = {
    val colorCode = card.color.toLowerCase match {
      case "red" => Red
      case "green" => Green
      case "blue" => Blue
      case "yellow" => Yellow
      case _ => Reset
    }

    card match {
      case NumberCard(color, value) =>
        println(s"${colorCode}NumberCard($color, $value)$Reset")
      case ActionCard(color, action) =>
        println(s"${colorCode}ActionCard($color, $action)$Reset")

      case  wild: WildCard =>
        val actionString = wild.action match {
          case "wild" => "Wild Card"
          case "wild draw four" => "Wild Draw Four"
        }
    }
  }
}

