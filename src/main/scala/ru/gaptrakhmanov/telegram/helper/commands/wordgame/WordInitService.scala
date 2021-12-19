package ru.gaptrakhmanov.telegram.helper.commands.wordgame

import cats.effect.Sync
import cats.implicits._
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.dto.{User, WordSet}
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState
import ru.gaptrakhmanov.telegram.helper.util.UrlUtils

object WordInitService {

  private val wordsUrl = "https://random-word-api.herokuapp.com/word?number=3"

  private val getStrs = "\"([^, ]+)\"".r

  private def getWordsFromUrl[F[_] : Sync]: F[Set[String]] = for {
    str <- UrlUtils.getDataFromUrl[F](wordsUrl)
  } yield (for (m <- getStrs findAllMatchIn str) yield m group 1).toSet

  def getWords[F[_] : Sync](state: WordGameState[F], id: Long): F[Set[String]] = {
    val user = User(id)
    state.isEmptySet(user).ifM(
      getWordsFromUrl.flatTap(words => state.saveWordSet(user, WordSet(words))),
      state.getWordSet(user).map(_.words)
    )
  }

  def resetWords[F[_] : Sync](state: WordGameState[F], id: Long): F[Set[String]] =
    state.resetWordSet(User(id)).productR(getWords(state, id))
}
