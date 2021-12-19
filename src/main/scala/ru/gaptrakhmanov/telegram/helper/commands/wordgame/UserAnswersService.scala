package ru.gaptrakhmanov.telegram.helper.commands.wordgame

import cats.effect.Sync
import cats.implicits._
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto.User
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState

object UserAnswersService {

  private def isConsist(word: String, set: Set[String]): Boolean = {
    val wordInfo = seqToMapOfElementToCout(word)
    val setInfo = seqToMapOfElementToCout(set.mkString)
    val containsAllChars = wordInfo.keys.toSet.subsetOf(setInfo.keys.toSet)
    containsAllChars && wordInfo.forall(map => setInfo(map._1) >= map._2)
  }

  private def seqToMapOfElementToCout[A](as: Seq[A]): Map[A, Int] =
    as.groupMapReduce(identity)(_ => 1)(_ + _)

  def setUserAnswer[F[_] : Sync](state: WordGameState[F], id: Long, word: String): F[String] = {
    val user = User(id)
    for {
      wordSet <- state.getWordSet(user)
      wordIsConsisted = isConsist(word, wordSet.words)
      answer <- if (wordIsConsisted) {
        ValidateWordService.isRealEnglishWord(word).ifM(
          state.saveUserAnswer(user, word).as("Cool! Correct!"),
          Sync[F].delay("Your answer should be a correct English word!")
        )
      } else {
        Sync[F].delay("Your answer should be constructed from letters of given words!")
      }
    } yield answer
  }

  def getUserAnswers[F[_] : Sync](state: WordGameState[F], id: Long): F[Set[String]] = {
    state.getUserAnswers(User(id)).map(_.answers)
  }

}
