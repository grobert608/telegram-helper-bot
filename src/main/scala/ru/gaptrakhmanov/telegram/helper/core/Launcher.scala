package ru.gaptrakhmanov.telegram.helper.core

import cats.effect.{ExitCode, IO, IOApp}
import ru.gaptrakhmanov.telegram.helper.commands.notes.repository.DoobieUserRepository
import ru.gaptrakhmanov.telegram.helper.commands.wordgame.state.WordGameState

import scala.util.{Failure, Success, Try}

object Launcher extends IOApp {

  val token: Try[String] = getProperties.map(_.getProperty("token"))

  def run(args: List[String]): IO[ExitCode] = {
    token match {
      case Failure(_) => IO(ExitCode.Error)
      case Success(token) => for {
        _ <- DbTransactor.make[IO].use { xa =>
          for {
            st <- WordGameState.create[IO]()
            ur <- DoobieUserRepository.create[IO](xa)
            _ <- ur.createTable
            _ <- new HelperBot[IO](token, st, ur).startPolling()
          } yield ()
        }
      } yield ExitCode.Success
    }
  }
}
