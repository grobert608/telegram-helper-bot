package ru.gaptrakhmanov.telegram.helper.core

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.Transactor
import ru.gaptrakhmanov.telegram.helper.core.DbConfig.{dbDriverName, dbPwd, dbUrl, dbUser}

object DbTransactor {
  
  def make[F[_]: ContextShift: Async]: Resource[F, Transactor[F]] =
    Blocker[F].map { be =>
      Transactor.fromDriverManager[F](
        driver = dbDriverName,
        url = dbUrl,
        user = dbUser,
        pass = dbPwd,
        blocker = be,
      )
    }
}
