package ru.gaptrakhmanov.telegram.helper.notification

trait BuildInfoNotifyer[F[_], BI] {
  def notifyUser(buildInfo: BI): F[Unit]
}
