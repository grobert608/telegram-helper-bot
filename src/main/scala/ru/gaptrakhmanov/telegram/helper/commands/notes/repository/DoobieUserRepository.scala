package ru.gaptrakhmanov.telegram.helper.commands.notes.repository

import cats.effect.Async
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import ru.gaptrakhmanov.telegram.helper.commands.notes.model.User

trait DoobieUserRepository[F[_]] extends UserRepository[F] {
  protected def transactor: Transactor[F]
}

object SQL {
  def createTable: Update0 =
    sql""" CREATE TABLE IF NOT EXISTS Users (id varchar, message varchar)""".update

  def listAllMessages(id: String): Query0[String] =
    sql"""SELECT message FROM Users u where LOWER(u.id) = ${id.toLowerCase}""".query[String]

  def insertUser(u: User): Update0 =
    sql"""INSERT INTO Users (id, message) VALUES (${u.id}, ${u.message})""".update

  def deleteMessage(u: User): Update0 =
    sql"""DELETE FROM Users us where LOWER(us.id) = ${u.id.toLowerCase} AND LOWER(us.message) = ${u.message.toLowerCase}""".update

  def deleteAllMessage(id: String): Update0 =
    sql"""DELETE FROM Users us where LOWER(us.id) = ${id.toLowerCase}""".update
}

object DoobieUserRepository {

  def create[F[_] : Async](xa: Transactor[F]): F[DoobieUserRepository[F]] =
    Async[F].delay(new DoobieUserRepository[F] {
      override protected def transactor: Transactor[F] = xa

      override def createTable: F[Unit] =
        Async[F].void(SQL
          .createTable
          .run
          .transact(transactor))

      override def listAllMessages(id: Long): F[List[String]] =
        SQL
          .listAllMessages(String.valueOf(id))
          .to[List]
          .transact(transactor)

      override def insertUser(u: User): F[Unit] =
        Async[F].void(SQL
          .insertUser(u)
          .run
          .transact(transactor))

      override def deleteMessage(u: User): F[Unit] =
        Async[F].void(SQL
          .deleteMessage(u)
          .run
          .transact(transactor))

      override def deleteAllMessage(id: Long): F[Unit] =
        Async[F].void(SQL
          .deleteAllMessage(String.valueOf(id))
          .run
          .transact(transactor))
    }
    )
}
