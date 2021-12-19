package ru.gaptrakhmanov.telegram.helper.core

import cats.effect.{Concurrent, ContextShift, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import org.asynchttpclient.Dsl.asyncHttpClient
import ru.gaptrakhmanov.telegram.helper.commands.jokes.JokeService
import ru.gaptrakhmanov.telegram.helper.commands.weather.WeatherService
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.{UserAnswersService, WordInitService}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

class HelperBot[F[_] : Concurrent : Timer : ContextShift](token: String, state: WordGameState[F])
  extends TelegramBot[F](token, AsyncHttpClientCatsBackend.usingClient[F](asyncHttpClient()))
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {

  onCommand("/help") { implicit msg =>
    reply(
      """
        |Common commands:
        |/weather - get wheather in Lisbon on Russian
        |/joke - get random joke about Chuck Norris
        |Word game commands:
        |/gameRule - show rule of word game
        |/getWords - show 3 initial words
        |/resetWords - reset 3 initial words and show
        |/setAnswer - try to set word which might be created from letters of initial words
        |/answers - show all user answers
        |""".stripMargin).void
  }

  onCommand("/gameRule") { implicit msg =>
    reply(
      """
        |You need to get 3 initial word by command /getWords or /resetWords
        |So, you should create new words from letters of initial words
        |You can check your word by command /setAnswer
        |""".stripMargin).void
  }

  onCommand("/weather") { implicit msg =>
    WeatherService.getWeather.flatMap(weather => reply(weather)).void
  }

  onCommand("/joke") { implicit msg =>
    JokeService.getJoke.flatMap(joke => reply(joke)).void
  }

  onCommand("/getWords") { implicit msg =>
    using(_.from) {
      user =>
        WordInitService.getWords(state, user.id).flatMap(words => reply(words.mkString("\n"))).void
    }
  }

  onCommand("/resetWords") { implicit msg =>
    using(_.from) {
      user =>
        WordInitService.resetWords(state, user.id).flatMap(words => reply(words.mkString("\n"))).void
    }
  }

  onCommand("/setAnswer") { implicit msg =>
    using(_.from) {
      user =>
        withArgs {
          case Seq(word) =>
            UserAnswersService.setUserAnswer(state, user.id, word).flatMap(words => reply(words)).void
          case _ =>
            reply("Invalid argument. Usage: /setAnswer apple").void
        }
    }
  }

  onCommand("/answers") { implicit msg =>
    using(_.from) {
      user =>
        UserAnswersService.getUserAnswers(state, user.id).flatMap(answers => reply(
          if (answers.isEmpty) {
            "You have not sent any correct answers!"
          } else {
            answers.mkString("\n")
          }).void
        )
    }
  }
}
