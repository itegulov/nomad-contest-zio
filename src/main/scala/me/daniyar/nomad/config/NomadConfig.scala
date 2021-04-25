package me.daniyar.nomad.config

import pureconfig._
import zio.{ZIO, ZLayer}

object NomadConfig:
  final case class Config(http: HttpConfig.Config, database: DatabaseConfig.Config)

  object Config:
    implicit val reader: ConfigReader[Config] =
      ConfigReader.forProduct2("http", "database")(Config.apply)
    implicit val writer: ConfigWriter[Config] =
      ConfigWriter.forProduct2("http", "database")(c => (c.http, c.database))

  val live: ZLayer[Any, IllegalStateException, NomadConfig] =
    ZLayer.fromEffect {
      ZIO
        .fromEither(ConfigSource.default.load[Config])
        .mapError(failures =>
          new IllegalStateException(
            s"Error loading configuration: $failures"
          )
        )
    }
