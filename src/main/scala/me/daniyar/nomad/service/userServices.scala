package me.daniyar.nomad.service

import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import me.daniyar.nomad.error.{
  DbException,
  EmailIsTakenException,
  InvalidPasswordException,
  SignupException,
  UserNotFoundException
}
import me.daniyar.nomad.model.{User, UserId, UserLoginInfo, UserSignupInfo}
import me.daniyar.nomad.repository.UserRepository
import me.daniyar.nomad.util.Transactor
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.Http4sDsl
import pdi.jwt.Jwt
import zio.{RIO, Task, ZIO}
import zio.interop.catz._

import scala.util.Try

def userServices[R <: UserRepository]: HttpRoutes[RIO[R, *]] =
  type UserTask[A] = RIO[R, A]

  val dsl = Http4sDsl[UserTask]
  import dsl._

  def generateTokenResult(user: User): String =
    Jwt.encode(s"""{"user": ${user.id.get.value}""")

  val userFind = HttpRoutes.of[UserTask] { case GET -> Root / UserIdVar(id) =>
    val zio = for {
      userOpt <- UserRepository.findById(id)
      result  <- userOpt.map(user => Ok(user.asJson)).getOrElse(NotFound())
    } yield result

    zio.catchAll { case dbException: DbException =>
      InternalServerError()
    }
  }

  val userSignUp = HttpRoutes.of[UserTask] { case req @ POST -> Root / "signup" =>
    val zio = for {
      userSignupInfo <- req.as[UserSignupInfo]
      user           <- UserRepository.insert(userSignupInfo.asUser)
      tokenResult     = generateTokenResult(user)
      result         <- Created(tokenResult)
    } yield result

    zio.catchAll {
      case e: EmailIsTakenException =>
        BadRequest(s"Email ${e.email} is taken")
      case dbException: DbException =>
        InternalServerError()
    }
  }

  val userSignIn = HttpRoutes.of[UserTask] { case req @ POST -> Root / "login" =>
    val zio = for {
      loginInfo  <- req.as[UserLoginInfo]
      user       <- UserRepository.authenticate(loginInfo)
      tokenResult = generateTokenResult(user)
      result     <- Ok(tokenResult)
    } yield result

    zio.catchAll {
      case e: (UserNotFoundException | InvalidPasswordException) =>
        BadRequest("Invalid username/password")
      case dbException: DbException                              =>
        InternalServerError()
    }
  }

  userFind <+> userSignUp <+> userSignIn
