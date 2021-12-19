package ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto

case class WordSet(words: Set[String])

object WordSet {
  def empty: WordSet = WordSet(Set.empty)
}
