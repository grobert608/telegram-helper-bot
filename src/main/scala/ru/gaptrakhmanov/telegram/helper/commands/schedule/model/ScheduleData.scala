package ru.gaptrakhmanov.telegram.helper.commands.schedule.model

import java.time.LocalDateTime

final case class ScheduleData(chatId: Long, text: String, time: LocalDateTime)
