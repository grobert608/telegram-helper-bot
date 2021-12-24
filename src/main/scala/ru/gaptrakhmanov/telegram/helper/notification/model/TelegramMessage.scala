package ru.gaptrakhmanov.telegram.helper.notification.model

trait TelegramMessage {
  def chatId: Long
  def text: String
}
