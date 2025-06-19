package de.htwg.se.uno.model.fileIOComponent.fileIOXML

import de.htwg.se.uno.model.cardComponent.{ActionCard, Card, NumberCard, WildCard}
import de.htwg.se.uno.model.fileIOComponent.FileIOInterface
import de.htwg.se.uno.model.gameComponent.GameStateInterface
import de.htwg.se.uno.model.gameComponent.base.GameState
import de.htwg.se.uno.model.gameComponent.base.state.GamePhase
import de.htwg.se.uno.model.playerComponent.PlayerHand

import java.io.{File, PrintWriter}
import scala.xml.{Elem, Node, PrettyPrinter, XML}

class FileIOXml extends FileIOInterface {
  private val savedir = "src/main/scala/de/htwg/se/uno/model/fileIOComponent/data/"
  override def save(gameState: GameStateInterface, file: String = "Uno.xml"): Unit = {
    new File(savedir).mkdirs()
    val xmlFile = new File(savedir + file)
    val pw = new PrintWriter(xmlFile)
    val prettyPrinter = new PrettyPrinter(120,4)
    val xml = prettyPrinter.format(gameStateToXml(gameState.asInstanceOf[GameState]))
    pw.write(xml)
    pw.close()
  }

  override def load(file: String = "Uno.xml"): GameStateInterface = {
    val xmlFile = new File(savedir + file)
    val loadFileXml = XML.loadFile(xmlFile)
    gameStateFromXml(loadFileXml)
  }

  private def gameStateToXml(game: GameState): Elem = {
    <unoGame>
      <players>
        {game.players.map(playerHandToXml)}
      </players>
      <currentPlayerIndex>
        {game.currentPlayerIndex}
      </currentPlayerIndex>
      <isReversed>
        {game.isReversed}
      </isReversed>
      <discardPile>
        {game.discardPile.map(cardToXml)}
      </discardPile>
      <drawPile>
        {game.drawPile.map(cardToXml)}
      </drawPile>
      <selectedColor>
        {game.selectedColor.getOrElse("")}
      </selectedColor>
      <currentPhase>
        {game.currentPhase.map(_.toString).getOrElse("")}
      </currentPhase>
    </unoGame>
  }

  private def gameStateFromXml(node: Node): GameState = {
    val players = (node \ "players" \ "playerHand").map(playerHandFromXml).toList
    val currentPlayerIndex = (node \ "currentPlayerIndex").text.toInt
    val isReversed = (node \ "isReversed").text.toBoolean
    val discardPile = (node \ "discardPile" \ "card").map(cardFromXml).toList
    val drawPile = (node \ "drawPile" \ "card").map(cardFromXml).toList
    val selectedColor = (node \ "selectedColor").text match {
      case "" => None
      case color => Some(color)
    }
    val currentPhase = (node \ "currentPhase").text match {
      case "" => None
      case phase => Some(GamePhase.withName(phase))
    }

    GameState(players, currentPlayerIndex, allCards = discardPile ++ drawPile, isReversed, discardPile, drawPile, selectedColor, currentPhase)
  }

  private def playerHandToXml(playerHand: PlayerHand): Elem = {
    <playerHand>
      <hand>
        <cards>{playerHand.cards.map(cardToXml)}</cards>
      </hand>
      <hasSaidUno>{playerHand.hasSaidUno}</hasSaidUno>
    </playerHand>
  }

  private def playerHandFromXml(node: Node): PlayerHand = {
    val hand = (node \ "hand" \"cards" \ "card").map(cardFromXml).toList
    val hasSaidUno = (node \ "hasSaidUno").text.toBoolean
    PlayerHand(hand, hasSaidUno)
  }

  private def cardToXml(card: Card): Elem = card match {
    case NumberCard(color, number) =>
      <card type="NumberCard">
        <color>{color}</color>
        <number>{number}</number>
      </card>
    case ActionCard(color, action) =>
      <card type="ActionCard">
        <color>{color}</color>
        <action>{action}</action>
      </card>
    case WildCard(action) =>
      <card type="WildCard">
        <action>{action}</action>
      </card>
  }

  private def cardFromXml(node: Node): Card = {
    (node \ "@type").text match {
      case "NumberCard" =>
        val color = (node \ "color").text
        val number = (node \ "number").text.toInt
        NumberCard(color, number)
      case "ActionCard" =>
        val color = (node \ "color").text
        val action = (node \ "action").text
        ActionCard(color, action)
      case "WildCard" =>
        val action = (node \ "action").text
        WildCard(action)
    }
  }
}
