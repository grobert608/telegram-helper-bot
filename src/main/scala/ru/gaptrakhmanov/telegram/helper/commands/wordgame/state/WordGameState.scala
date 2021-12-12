package ru.gaptrakhmanov.telegram.helper.commands.wordgame.state

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto.{GameInfo, User, UsersAnswers, WordSet}

trait WordGameState[F[_]] {

  def saveWordSet(user: User, words: WordSet): F[Unit]

  def getWordSet(user: User): F[WordSet]

  def isEmptySet(user: User): F[Boolean]

  def resetWordSet(user: User): F[Unit]

  def saveUserAnswer(user: User, word: String): F[Unit]

  def getUserAnswers(user: User): F[UsersAnswers]
}

object WordGameState {
  def create[F[_] : Sync](): F[WordGameState[F]] =
    Ref.of[F, Map[User, GameInfo]](Map.empty).map { ref =>
      new WordGameState[F] {
        override def saveWordSet(user: User, words: WordSet): F[Unit] =
          ref.update(_ + (user -> GameInfo(words, UsersAnswers.empty)))

        override def getWordSet(user: User): F[WordSet] =
          ref.get.map(_.getOrElse(user, GameInfo.empty).wordSet)

        override def isEmptySet(user: User): F[Boolean] =
          ref.get.map(_.getOrElse(user, GameInfo.empty).wordSet.set.isEmpty)

        override def resetWordSet(user: User): F[Unit] =
          ref.update(_.removed(user))

        override def saveUserAnswer(user: User, word: String): F[Unit] =
          ref.update(m => {
            val gi = m.getOrElse(user, GameInfo.empty)
            val uaSet = gi.usersAnswers.set + word
            m + (user -> gi.copy(usersAnswers = UsersAnswers(uaSet)))
          })

        override def getUserAnswers(user: User): F[UsersAnswers] =
          ref.get.map(_.getOrElse(user, GameInfo.empty).usersAnswers)
      }
    }
}
