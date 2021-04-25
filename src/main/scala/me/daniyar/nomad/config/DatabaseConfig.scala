package me.daniyar.nomad.config

import pureconfig._
import zio.ZLayer

object DatabaseConfig:
  final case class Config(url: String, driver: String, user: String, password: String)

  object Config:
    implicit val reader: ConfigReader[Config] =
      ConfigReader.forProduct4("url", "driver", "user", "password")(Config.apply)
    implicit val writer: ConfigWriter[Config] =
      ConfigWriter.forProduct4("url", "driver", "user", "password")(c =>
        (c.url, c.driver, c.user, c.password)
      )

  val fromNomadConfig: ZLayer[NomadConfig, Nothing, DatabaseConfig] =
    ZLayer.fromService(_.database)
