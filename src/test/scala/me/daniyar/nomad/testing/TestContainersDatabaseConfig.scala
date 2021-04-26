package me.daniyar.nomad.testing

import me.daniyar.nomad.config.DatabaseConfig
import zio.ZLayer

object TestContainersDatabaseConfig:
  def test: ZLayer[PostgresqlDocker, Nothing, DatabaseConfig] =
    ZLayer.fromService { container =>
      DatabaseConfig.Config(
        container.jdbcUrl,
        "org.postgresql.Driver",
        container.username,
        container.password
      )
    }
