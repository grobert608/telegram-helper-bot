package ru.gaptrakhmanov.telegram.helper.core

object DbConfig {
  val dbHost: String = System.getenv("DB_HOST")
  val dbDriverName: String = "org.postgresql.Driver"
  val dbUrl: String =  s"jdbc:postgresql://$dbHost:5432/"
  val dbUser: String = getProperties.map(_.getProperty("dbUser")).getOrElse("postgres")
  val dbPwd: String = getProperties.map(_.getProperty("dbPwd")).getOrElse("123456")
}
