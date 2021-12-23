package ru.gaptrakhmanov.telegram.helper.commands.weather

import cats.effect.Sync
import cats.implicits._
import org.jsoup.Jsoup

object WeatherService {

  private val city = "lisbon"

  private val weatherUrl = "https://world-weather.ru/pogoda/portugal/%s/"

  def getWeather[F[_] : Sync]: F[String] =
    Sync[F].delay(Jsoup.connect(String.format(weatherUrl, city)).get)
      .map(_.select("span.dw-into").text.split("Подробнее")(0))
      .recover(_ => "\"Failed to get weather!\"")
}
