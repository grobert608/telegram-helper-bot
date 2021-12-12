package ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto

case class UsersAnswers(set: Set[String])

object UsersAnswers {
  def empty: UsersAnswers = UsersAnswers(Set.empty)
}
