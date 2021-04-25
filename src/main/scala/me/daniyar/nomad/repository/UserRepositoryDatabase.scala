package me.daniyar.nomad.repository

import cats.implicits._
import doobie.{ConnectionIO, Transactor}
import doobie.implicits._
import me.daniyar.nomad.error.{
  AuthenticationException,
  DbException,
  EmailIsTakenException,
  InvalidPasswordException,
  SignupException,
  UserNotFoundException
}
import me.daniyar.nomad.model.{User, UserId, UserLoginInfo}
import me.daniyar.nomad.util.transaction
import org.mindrot.jbcrypt.BCrypt
import zio.{Has, IO, Task, UIO, ZIO}

class UserRepositoryDatabase(private implicit val transactor: Transactor[Task])
    extends UserRepository.Service:
  override def findById(id: UserId): IO[DbException, Option[User]] =
    transaction(UserRepositoryDatabase.Sql.findById(id))

  override def findByEmail(email: String): IO[DbException, Option[User]] =
    transaction(UserRepositoryDatabase.Sql.findByEmail(email))

  override def authenticate(
    loginInfo: UserLoginInfo
  ): IO[DbException | AuthenticationException, User] =
    for {
      userOpt      <- transaction(UserRepositoryDatabase.Sql.findByEmail(loginInfo.email))
      verifiedUser <- userOpt match {
                        case Some(user) =>
                          if (BCrypt.checkpw(loginInfo.password, user.password)) {
                            ZIO.succeed(user)
                          } else {
                            ZIO.fail(InvalidPasswordException(loginInfo.email))
                          }
                        case None       =>
                          ZIO.fail(UserNotFoundException(loginInfo.email))
                      }
    } yield verifiedUser

  override def insert(user: User): IO[DbException | SignupException, User] =
    val encryptedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
    transaction {
      for {
        existingUserOpt <- UserRepositoryDatabase.Sql.findByEmail(user.email)
        insertedUser    <- existingUserOpt match {
                             case Some(_) =>
                               EmailIsTakenException(user.email).asLeft[User].pure[ConnectionIO]
                             case None    =>
                               UserRepositoryDatabase.Sql
                                 .insert(user, encryptedPassword)
                                 .map(_.asRight[SignupException])
                           }
      } yield insertedUser
    }.absolve

object UserRepositoryDatabase:
  object Sql:
    def list: ConnectionIO[List[User]] =
      sql"select * from users"
        .query[User]
        .to[List]

    def findById(id: UserId): ConnectionIO[Option[User]] =
      sql"select * from users where user_id = ${id.value}"
        .query[User]
        .option

    def findByEmail(email: String): ConnectionIO[Option[User]] =
      sql"select * from users where user_email = $email"
        .query[User]
        .option

    def insert(
      user: User,
      encryptedPassword: String
    ): ConnectionIO[User] =
      sql"""
          insert into users (user_first_name, user_last_name, user_email, user_encrypted_password)
          values (${user.firstName}, ${user.lastName}, ${user.email}, ${encryptedPassword: String})
        """.update
        .withUniqueGeneratedKeys[UserId]("user_id")
        .map(userId => user.copy(id = Some(userId)))
