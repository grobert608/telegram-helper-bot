package ru.gaptrakhmanov.telegram.helper.core

object DbConfig {
  val dbDriverName: String = "org.postgresql.Driver"
  val dbUrl: String = getProperties.map(_.getProperty("dbUrl")).getOrElse("jdbc:postgresql://localhost:5432/")
  val dbUser: String = getProperties.map(_.getProperty("dbUser")).getOrElse("postgres")
  val dbPwd: String = getProperties.map(_.getProperty("dbPwd")).getOrElse("123456")
}
