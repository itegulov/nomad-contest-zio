package me.daniyar.nomad.testing

import com.dimafeng.testcontainers.PostgreSQLContainer
import zio.blocking.{effectBlocking, Blocking}
import zio.{Has, ZIO, ZLayer, ZManaged}

type PostgresqlDocker = Has[PostgreSQLContainer]

object PostgresqlDocker:
  val live: ZLayer[Blocking, Nothing, PostgresqlDocker] =
    ZManaged.make {
      effectBlocking {
        val container = new PostgreSQLContainer()
        container.start()
        container
      }.orDie
    }(container => effectBlocking(container.stop()).orDie).toLayer
