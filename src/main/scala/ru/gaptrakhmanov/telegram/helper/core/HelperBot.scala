package ru.gaptrakhmanov.telegram.helper.core

import cats.effect.{Concurrent, ContextShift, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import org.asynchttpclient.Dsl.asyncHttpClient
import ru.gaptrakhmanov.telegram.helper.commands.jokes.JokeService
import ru.gaptrakhmanov.telegram.helper.commands.notes.model.User
import ru.gaptrakhmanov.telegram.helper.commands.notes.repository.UserRepository
import ru.gaptrakhmanov.telegram.helper.commands.schedule.model.ScheduleData
import ru.gaptrakhmanov.telegram.helper.commands.schedule.repository.ScheduleRepository
import ru.gaptrakhmanov.telegram.helper.commands.weather.WeatherService
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.{UserAnswersService, WordInitService}
import ru.gaptrakhmanov.telegram.helper.notification.BuildInfoNotifyer
import ru.gaptrakhmanov.telegram.helper.notification.model.{BuildResultNotification, TelegramMessage}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import java.time.LocalDateTime
import scala.util.Try

class HelperBot[F[_] : Concurrent : ContextShift, N <: TelegramMessage](token: String,
                                                                        state: WordGameState[F],
                                                                        userRepository: UserRepository[F],
                                                                        scheduleRepository: ScheduleRepository[F])
  extends TelegramBot[F](token, AsyncHttpClientCatsBackend.usingClient[F](asyncHttpClient()))
    with Polling[F]
    with Commands[F]
    with RegexCommands[F]
    with BuildInfoNotifyer[F, N] {

  object Int {
    def unapply(s: String): Option[Int] = Try(s.toInt).toOption
  }

  override def notifyUser(notification: N): F[Unit] = for {
    _ <- request(
      SendMessage(
        notification.chatId,
        notification.text
      )
    )
  } yield ()

  onCommand("/help") { implicit msg =>
    reply(
      """
        |Common commands:
        |/weather - get weather in Lisbon on Russian
        |/joke - get random joke about Chuck Norris
        |Notes:
        |/noteAdd {text} - add new note
        |/noteShowAll - show all notes
        |/noteDelete {text} - delete note with special text
        |/noteDeleteAll - delete all notes
        |Word game commands:
        |/gameRule - show rule of word game
        |/getWords - show 3 initial words
        |/resetWords - reset 3 initial words and show
        |/setAnswer {word} - try to set word which might be created from letters of initial words
        |/answers - show all user answers
        |Notification:
        |/notificationAdd {data - 2021-12-16_15:00} {text} - set notification
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

  onCommand("/noteAdd") { implicit msg =>
    using(_.from) {
      user =>
        withArgs {
          case words =>
            userRepository.insertUser(User(String.valueOf(user.id), words.mkString(" "))).flatMap(_ =>
              reply("Saved").void)
          case _ =>
            reply("Invalid argument. Usage: /noteAdd buy apples").void
        }
    }
  }

  onCommand("/noteShowAll") { implicit msg =>
    using(_.from) {
      user =>
        userRepository.listAllMessages(user.id).flatMap(
          words => reply(
            if (words.isEmpty) {
              "You have not added any notes!"
            } else {
              words.mkString("\n")
            }
          ).void
        )
    }
  }

  onCommand("/noteDelete") { implicit msg =>
    using(_.from) {
      user =>
        withArgs {
          case words =>
            userRepository.deleteMessage(User(String.valueOf(user.id), words.mkString(" "))).flatMap(_ =>
              reply("Deleted").void)
          case _ =>
            reply("Invalid argument. Usage: /noteDelete buy apples").void
        }
    }
  }

  onCommand("/noteDeleteAll") { implicit msg =>
    using(_.from) {
      user =>
        userRepository.deleteAllMessage(user.id).flatMap(_ =>
          reply("All deleted").void)
    }
  }

  onRegex("""/notificationAdd\s+([1-2][0-9][0-9][0-9])-([0-1]?[0-2])-([0-3]?[0-9])_([0-5]?[0-9]):([0-5]?[0-9])\s+([a-zA-Z\s]*)""".r) { implicit msg => {
    case Seq(Int(year), Int(month), Int(date), Int(hh), Int(mm), text) =>
      scheduleRepository.saveNewTask(ScheduleData(
        msg.chat.id,
        text,
        LocalDateTime.of(year, month, date, hh, mm, 0))).flatMap(_ => reply("Task saved!")).void
  }
  }
}

object HelperBot {
  def create[F[_] : Concurrent : ContextShift, N <: TelegramMessage](token: String,
                                                                     state: WordGameState[F],
                                                                     userRepository: UserRepository[F],
                                                                     scheduleRepository: ScheduleRepository[F]): F[HelperBot[F, N]] = {
    Sync[F].delay(new HelperBot(token, state, userRepository, scheduleRepository))
  }
}
