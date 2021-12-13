package ru.gaptrakhmanov.telegram.helper.commands.notes.repository

import ru.gaptrakhmanov.telegram.helper.commands.notes.model.User

trait UserRepository[F[_]] {
  def createTable: F[Unit]

  def insertUser(u: User): F[Unit]

  def listAllMessages(id: Long): F[List[String]]

  def deleteMessage(u: User): F[Unit]

  def deleteAllMessage(id: Long): F[Unit]
}
