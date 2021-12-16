package ru.gaptrakhmanov.telegram.helper.commands.schedule.repository

import ru.gaptrakhmanov.telegram.helper.commands.schedule.model.ScheduleData

import java.time.LocalDateTime

trait ScheduleRepository[F[_]] {
  def createTable: F[Unit]

  def saveNewTask(sd: ScheduleData): F[Unit]

  def getAllReadyTasks(date: LocalDateTime): F[List[ScheduleData]]

  def deleteAllPastTasks(date: LocalDateTime): F[Unit]
}
