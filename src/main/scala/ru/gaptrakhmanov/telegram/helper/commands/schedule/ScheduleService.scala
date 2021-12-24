package ru.gaptrakhmanov.telegram.helper.commands.schedule

import cats.effect.{Concurrent, ContextShift, Sync, Timer}
import cats.implicits._
import ru.gaptrakhmanov.telegram.helper.commands.schedule.repository.ScheduleRepository
import ru.gaptrakhmanov.telegram.helper.core.HelperBot
import ru.gaptrakhmanov.telegram.helper.notification.model.BuildResultNotification

import java.time.LocalDateTime
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, MINUTES}

class ScheduleService[F[_] : Concurrent : ContextShift : Timer](scheduleRepository: ScheduleRepository[F], bot: HelperBot[F, BuildResultNotification]) {

  def repeat(io: F[Unit]): F[Unit] = io >> Timer[F].sleep(FiniteDuration(1, MINUTES)) >> repeat(io)

  private val threadPoll = Executors.newFixedThreadPool(1)
  private val executionContext = ExecutionContext.fromExecutor(threadPoll)

  def run: F[Unit] = ContextShift[F].evalOn(executionContext)(repeat({
    val date = LocalDateTime.now()
    for {
      list <- scheduleRepository.getAllReadyTasks(date)
      _ <- list.flatTraverse(data => {
        bot.notifyUser(BuildResultNotification(data.chatId, "New notification: \n" + data.text)) *> Sync[F].delay(List(data))
      })
      _ <- scheduleRepository.deleteAllPastTasks(date)
    } yield ()
  })
  )
}

object ScheduleService {
  def create[F[_] : Concurrent : ContextShift : Timer](scheduleRepository: ScheduleRepository[F],
                                                       bot: HelperBot[F, BuildResultNotification]): F[ScheduleService[F]] = {
    Sync[F].delay(new ScheduleService(scheduleRepository, bot))
  }
}

