package ru.gaptrakhmanov.telegram.helper.notification.model

case class BuildResultNotification(chatId: Long, text: String) extends TelegramMessage
