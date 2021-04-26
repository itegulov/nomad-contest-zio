package me.daniyar.nomad.repository

import cats.implicits._
import doobie.{ConnectionIO, Transactor}
import doobie.implicits._
// We need legacy instant instances for Postgres: https://github.com/tpolecat/doobie/releases/tag/v0.8.8
import doobie.implicits.legacy.instant._
import me.daniyar.nomad.error.DbException
import me.daniyar.nomad.model.{AugmentedJwt, JwtId, UserId}
import me.daniyar.nomad.util.transaction
import zio.{IO, Task}

class JwtRepositoryDatabase(private implicit val transactor: Transactor[Task])
    extends JwtRepository.Service:
  override def findById(id: JwtId): IO[DbException, Option[AugmentedJwt]] =
    transaction(JwtRepositoryDatabase.Sql.findById(id))

  override def findByUserId(userId: UserId): IO[DbException, Option[AugmentedJwt]] =
    transaction(JwtRepositoryDatabase.Sql.findByUserId(userId))

  override def insert(jwt: AugmentedJwt): IO[DbException, AugmentedJwt] =
    transaction(JwtRepositoryDatabase.Sql.insert(jwt))

object JwtRepositoryDatabase:
  object Sql:
    def findById(id: JwtId): ConnectionIO[Option[AugmentedJwt]] =
      sql"select * from jwt_tokens where id = ${id.value}"
        .query[AugmentedJwt]
        .option

    def findByUserId(id: UserId): ConnectionIO[Option[AugmentedJwt]] =
      sql"select * from jwt_tokens where user_id = ${id.value}"
        .query[AugmentedJwt]
        .option

    def insert(jwt: AugmentedJwt): ConnectionIO[AugmentedJwt] =
      sql"""
          insert into jwt_tokens (id, jwt, user_id, expiry, last_touched)
          values (${jwt.id}, ${jwt.jwt}, ${jwt.userId}, ${jwt.expiry}, ${jwt.lastTouched})
        """.update.run.as(jwt)
