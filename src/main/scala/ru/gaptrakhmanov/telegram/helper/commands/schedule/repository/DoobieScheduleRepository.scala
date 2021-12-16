package ru.gaptrakhmanov.telegram.helper.commands.schedule.repository

import cats.effect.Async
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.postgres.pgisimplicits._
import ru.gaptrakhmanov.telegram.helper.commands.schedule.model.ScheduleData

import java.sql.Timestamp
import java.time.LocalDateTime

trait DoobieScheduleRepository[F[_]] extends ScheduleRepository[F] {
  protected def transactor: Transactor[F]
}

object SQL {
  def createTable: Update0 =
    sql""" CREATE TABLE IF NOT EXISTS Schedule (chat_id VARCHAR, message VARCHAR, time TIMESTAMP)""".update

  def saveNewTask(sd: ScheduleData): Update0 =
    sql"""INSERT INTO Schedule (chat_id, message, time) VALUES (${sd.chatId}, ${sd.text}, ${Timestamp.valueOf(sd.time)})""".update

  def getAllReadyTasks(date: LocalDateTime): Query0[ScheduleData] =
    sql"""SELECT chat_id, message, time FROM Schedule sch where sch.time < ${Timestamp.valueOf(date)}""".query[ScheduleData]

  def deleteAllPastTasks(date: LocalDateTime): Update0 =
    sql"""DELETE FROM Schedule sch where sch.time < ${Timestamp.valueOf(date)}""".update
}

object DoobieScheduleRepository {

  def create[F[_] : Async](xa: Transactor[F]): F[DoobieScheduleRepository[F]] =
    Async[F].delay(new DoobieScheduleRepository[F] {
      override protected def transactor: Transactor[F] = xa

      override def createTable: F[Unit] =
        Async[F].void(SQL
          .createTable
          .run
          .transact(transactor))

      override def saveNewTask(sd: ScheduleData): F[Unit] =
        Async[F].void(SQL
          .saveNewTask(sd)
          .run
          .transact(transactor))

      override def getAllReadyTasks(date: LocalDateTime): F[List[ScheduleData]] =
        SQL
          .getAllReadyTasks(date)
          .to[List]
          .transact(transactor)

      override def deleteAllPastTasks(date: LocalDateTime): F[Unit] =
        Async[F].void(SQL
          .deleteAllPastTasks(date)
          .run
          .transact(transactor))
    }
    )
}
