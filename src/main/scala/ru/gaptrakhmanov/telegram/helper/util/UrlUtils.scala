package ru.gaptrakhmanov.telegram.helper.util

import cats.effect.{Resource, Sync}

import scala.io.{BufferedSource, Source}

object UrlUtils {

  def acquire[F[_] : Sync](url: String): F[BufferedSource] = Sync[F].delay(Source.fromURL(url))

  def release[F[_] : Sync](source: BufferedSource): F[Unit] = Sync[F].delay(source.close())

  def readSource[F[_] : Sync](source: Source): F[Iterator[String]] = Sync[F].delay(source.getLines())

  def urlResource[F[_] : Sync](url: String): Resource[F, Source] = Resource.make(acquire[F](url))(release[F])

  def getDataFromUrl[F[_] : Sync](url: String): F[String] =
    urlResource[F](url)
      .evalMap(readSource[F](_))
      .map(_.mkString)
      .use(str => Sync[F].delay(str))

}
