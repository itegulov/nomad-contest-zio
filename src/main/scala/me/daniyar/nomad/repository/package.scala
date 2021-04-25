package me.daniyar.nomad

import me.daniyar.nomad.error.{AuthenticationException, DbException, SignupException}
import me.daniyar.nomad.model.{User, UserId, UserLoginInfo}
import me.daniyar.nomad.util.Transactor
import zio.{Has, IO, UIO, ZIO, ZLayer}

package object repository:
  type UserRepository = Has[UserRepository.Service]

  object UserRepository:
    trait Service:
      def findById(id: UserId): IO[DbException, Option[User]]
      def findByEmail(email: String): IO[DbException, Option[User]]
      def authenticate(loginInfo: UserLoginInfo): IO[DbException | AuthenticationException, User]
      def insert(user: User): IO[DbException | SignupException, User]

    val layer: ZLayer[Transactor, Nothing, UserRepository] =
      ZLayer.fromService(transactor => new UserRepositoryDatabase()(transactor))

    def findById(id: UserId): ZIO[UserRepository, DbException, Option[User]] =
      ZIO.accessM(_.get.findById(id))

    def findByEmail(email: String): ZIO[UserRepository, DbException, Option[User]] =
      ZIO.accessM(_.get.findByEmail(email))

    def authenticate(
      loginInfo: UserLoginInfo
    ): ZIO[UserRepository, DbException | AuthenticationException, User] =
      ZIO.accessM(_.get.authenticate(loginInfo))

    def insert(user: User): ZIO[UserRepository, DbException | SignupException, User] =
      ZIO.accessM(_.get.insert(user))
