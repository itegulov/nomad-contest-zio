package me.daniyar.nomad.service

import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import me.daniyar.nomad.error.{DbException, EmailIsTakenException, InvalidPasswordException, SignupException, UserNotFoundException}
import me.daniyar.nomad.model.{AugmentedJwt, JwtId, User, UserId, UserLoginInfo, UserSignupInfo}
import me.daniyar.nomad.repository.{JwtRepository, UserRepository}
import me.daniyar.nomad.util.Transactor
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.Http4sDsl
import pdi.jwt.{Jwt, JwtClaim}
import zio.{RIO, Task, ZIO}
import zio.interop.catz._
import zio.random._

import java.time.Instant
import java.util.UUID
import scala.util.Try

def userServices[R <: UserRepository with JwtRepository with Random]: HttpRoutes[RIO[R, *]] =
  type UserTask[A] = RIO[R, A]

  val dsl = Http4sDsl[UserTask]
  import dsl._

  def generateTokenResult(user: User): UserTask[AugmentedJwt] =
    for {
      uuid        <- nextUUID
      jwtClaim     = JwtClaim(jwtId = Some(user.id.get.value.toString))
      jwt          = Jwt.encode(jwtClaim.toJson)
      augmentedJwt = AugmentedJwt(JwtId(uuid.toString), user.id.get, jwt, Instant.now(), None)
      result      <- JwtRepository.insert(augmentedJwt)
    } yield result

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
      jwt            <- generateTokenResult(user)
      result         <- Created(jwt.jwt)
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
      jwt        <- generateTokenResult(user)
      result     <- Ok(jwt.jwt)
    } yield result

    zio.catchAll {
      case e: (UserNotFoundException | InvalidPasswordException) =>
        BadRequest("Invalid username/password")
      case dbException: DbException                              =>
        InternalServerError()
    }
  }

  userFind <+> userSignUp <+> userSignIn
