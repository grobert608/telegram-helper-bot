package ru.gaptrakhmanov.telegram.helper

import java.util.Properties
import scala.util.Try

package object core {
  val getProperties: Try[Properties] = {
    val properties = new Properties()
    Try(properties.load(this.getClass.getClassLoader.getResourceAsStream("config.properties"))).map(_ => properties)
  }
}
