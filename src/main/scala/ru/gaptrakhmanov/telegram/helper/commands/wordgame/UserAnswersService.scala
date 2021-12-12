package ru.gaptrakhmanov.telegram.helper.commands.wordgame

import cats.effect.Sync
import cats.implicits._
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto.User
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState

object UserAnswersService {

  private def isConsist(word: String, set: Set[String]): Boolean = {
    val a = word.groupMapReduce(identity)(_ => 1)(_ + _)
    val b = set.mkString.trim.groupMapReduce(identity)(_ => 1)(_ + _)
    val containsAllChars = a.keys.toSet.subsetOf(b.keys.toSet)
    if (containsAllChars) {
      a.forall(map => {
        b(map._1) >= map._2
      })
    } else {
      false
    }
  }

  def setUserAnswer[F[_] : Sync](state: WordGameState[F], id: Long, word: String): F[String] = {
    val user = User(id)
    for {
      wordSet <- state.getWordSet(user)
      wordIsConsisted = isConsist(word, wordSet.set)
      answer <- if (wordIsConsisted) {
        for {
          wordIsReal <- ValidateWordService.isRealEnglishWord(word)
          ans <- if (wordIsReal) {
            for {
              _ <- state.saveUserAnswer(user, word)
            } yield "Cool! Correct!"
          } else {
            Sync[F].delay("Your answer should be a correct English word!")
          }
        } yield ans
      } else {
        Sync[F].delay("Your answer should be constructed from letters of given words!")
      }
    } yield answer
  }

  def getUserAnswers[F[_] : Sync](state: WordGameState[F], id: Long): F[Set[String]] = {
    val user = User(id)
    for {
      answers <- state.getUserAnswers(user)
    } yield answers.set
  }

}
