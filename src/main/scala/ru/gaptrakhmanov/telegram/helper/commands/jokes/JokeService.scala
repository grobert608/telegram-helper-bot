package ru.gaptrakhmanov.telegram.helper.commands.jokes

import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser
import ru.gaptrakhmanov.telegram.helper.util.UrlUtils

object JokeService {

  private case class Value(id: Int, joke: String, categories: List[String])

  private case class Joke(`type`: String, value: List[Value])

  private val jokeUrl = "http://api.icndb.com/jokes/random/1"

  def getJoke[F[_] : Sync]: F[String] =
    UrlUtils.getDataFromUrl[F](jokeUrl).map(parser.decode[Joke]).map {
      case Right(joke) => joke.value.head.joke
      case Left(_) => "Failed to get a joke!"
    }
}
