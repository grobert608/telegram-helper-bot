package ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto

case class GameInfo(wordSet: WordSet, usersAnswers: UsersAnswers)

object GameInfo {
  def empty: GameInfo = GameInfo(WordSet.empty, UsersAnswers.empty)
}
