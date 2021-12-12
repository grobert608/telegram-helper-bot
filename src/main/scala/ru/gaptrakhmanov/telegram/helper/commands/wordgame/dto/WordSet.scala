package ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto

case class WordSet(set: Set[String])

object WordSet {
  def empty: WordSet = WordSet(Set.empty)
}
