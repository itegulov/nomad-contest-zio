package me.daniyar.nomad.util

import cats.effect.Blocker
import doobie.{ConnectionIO, Transactor}
import doobie.hikari._
import doobie.implicits._
import me.daniyar.nomad.config._
import me.daniyar.nomad.error.DbException
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import zio.{Has, IO, Managed, Task, ZIO, ZLayer, ZManaged}
import zio.blocking.Blocking
import zio.interop.catz._

type Transactor = Has[doobie.Transactor[Task]]

object Transactor:
  def fromDatabaseConfig: ZLayer[Blocking with DatabaseConfig, Throwable, Transactor] =
    def initDb(cfg: DatabaseConfig.Config): Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(
      cfg: DatabaseConfig.Config
    ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- Managed.succeed(
                          rt.environment
                            .get[Blocking.Service]
                            .blockingExecutor
                            .asEC
                        )
          connectEC   = rt.platform.executor.asEC
          transactor <- HikariTransactor
                          .newHikariTransactor[Task](
                            cfg.driver,
                            cfg.url,
                            cfg.user,
                            cfg.password,
                            connectEC,
                            Blocker.liftExecutionContext(transactEC)
                          )
                          .toManaged
        } yield transactor
      }

    ZLayer.fromManaged {
      for {
        config     <- getDatabaseConfig.toManaged_
        _          <- initDb(config).toManaged_
        transactor <- mkTransactor(config)
      } yield transactor
    }

final def transaction[A](
  connectionIo: ConnectionIO[A]
)(implicit transactor: doobie.Transactor[Task]): IO[DbException, A] =
  connectionIo.transact(transactor).mapError(DbException.apply)
