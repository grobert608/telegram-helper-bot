package ru.gaptrakhmanov.telegram.helper.commands.wordgame

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser
import ru.gaptrakhmanov.telegram.helper.util.UrlUtils

object ValidateWordService {

  private case class Word(word: String)

  private val wordDescriptionUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/%s"

  def isRealEnglishWord[F[_] : Sync](word: String): F[Boolean] = for {
    wordDescriptionJson <- UrlUtils.getDataFromUrl[F](String.format(wordDescriptionUrl, word))
      .handleError(_ => "Error while getting word description!")
  } yield parser.decode[List[Word]](wordDescriptionJson) match {
    case Right(_) => true
    case Left(_) => false
  }

}
