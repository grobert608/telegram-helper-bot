package ru.gaptrakhmanov.telegram.helper.commands.weather

import cats.effect.Sync
import org.jsoup.Jsoup

import scala.util.Try

object WeatherService {

  private val city = "lisbon"

  private val weatherUrl = "https://world-weather.ru/pogoda/portugal/%s/"

  def getWeather[F[_] : Sync]: F[String] = {
    Sync[F].delay(
      Try(Jsoup.connect(String.format(weatherUrl, city)).get)
        .toOption.toRight("\"Failed to get weather!\"")
        .map(_.select("span.dw-into").text.split("Подробнее")(0)) match {
        case Right(doc) => doc
        case Left(error) => error
      })
  }
}
