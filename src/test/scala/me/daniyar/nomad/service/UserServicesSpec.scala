package me.daniyar.nomad.service

import io.circe._
import io.circe.parser._
import me.daniyar.nomad.repository.{JwtRepository, UserRepository}
import me.daniyar.nomad.service.userServices
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.Router
import pdi.jwt.Jwt
import zio._
import zio.interop.catz._
import zio.test._
import zio.test.Assertion._
import zio.random._

object UserServicesSpec extends DefaultRunnableSpec:
  type UserEnv = UserRepository with JwtRepository with Random
  type UserTask[A] = RIO[UserEnv, A]

  implicit val charset: Charset = Charset.`UTF-8`

  val app = Router[UserTask]("/" -> userServices).orNotFound

  val isValidJwt: Assertion[String] =
    Assertion.assertion("isValidJwt")() { actual =>
      Jwt.decode(actual).isSuccess
    }

  val testUserEmail    = "test@test.com"
  val testUserPassword = "12345"
  val testUserJson     =
    s"""{
       |  "email": "$testUserEmail",
       |  "firstName": "TestFirstName",
       |  "lastName": "TestLastName",
       |  "password": "$testUserPassword"
       |}""".stripMargin

  override def spec =
    suite("UserService")(
      testM("should sign up new users") {
        val req = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        checkRequest(
          app.run(req),
          Status.Created,
          isValidJwt
        )
      },
      testM("should sign in with existing credentials") {
        val signUpReq = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        val signInReq = request[UserTask](Method.POST, "/login").withEntity(
          s"""{
             |  "email": "$testUserEmail",
             |  "password": "$testUserPassword"
             |}""".stripMargin
        )
        val zio       = for {
          _        <- app.run(signUpReq)
          response <- app.run(signInReq)
        } yield response

        checkRequest(
          zio,
          Status.Ok,
          isValidJwt
        )
      },
      testM("should not sign in with incorrect email") {
        val signUpReq = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        val signInReq = request[UserTask](Method.POST, "/login").withEntity(
          s"""{
             |  "email": "test@nottest.com",
             |  "password": "$testUserPassword"
             |}""".stripMargin
        )
        val zio       = for {
          _        <- app.run(signUpReq)
          response <- app.run(signInReq)
        } yield response

        checkRequestEquals[UserEnv, String](
          zio,
          Status.BadRequest,
          "Invalid username/password"
        )
      },
      testM("should not sign in with incorrect password") {
        val signUpReq = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        val signInReq = request[UserTask](Method.POST, "/login").withEntity(
          s"""{
             |  "email": "$testUserEmail",
             |  "password": "123"
             |}""".stripMargin
        )
        val zio       = for {
          _        <- app.run(signUpReq)
          response <- app.run(signInReq)
        } yield response

        checkRequestEquals[UserEnv, String](
          zio,
          Status.BadRequest,
          "Invalid username/password"
        )
      },
      testM("should not allow signing up twice") {
        val signUpReq = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        val zio       = for {
          _        <- app.run(signUpReq)
          response <- app.run(signUpReq)
        } yield response

        checkRequestEquals[UserEnv, String](
          zio,
          Status.BadRequest,
          s"Email ${testUserEmail} is taken"
        )
      },
      testM("should find a signed up user by their id") {
        val zio = for {
          signUpRespR <- app.run(request[UserTask](Method.POST, "/signup").withEntity(testUserJson))
          signUpResp  <- signUpRespR.as[String]
          jwt          = Jwt.decode(signUpResp)
          userId       = Jwt.decode(signUpResp).toOption.flatMap(_.jwtId).get
          findReq     <- app.run(request[UserTask](Method.GET, s"/$userId"))
        } yield findReq

        checkRequestEquals[UserEnv, Json](
          zio,
          Status.Ok,
          parse(
            s"""{
               |  "id": 1,
               |  "firstName": "TestFirstName",
               |  "lastName": "TestLastName",
               |  "email": "$testUserEmail"
               |}""".stripMargin
          ).toOption.get
        )
      }
    ).provideSomeLayer[ZEnv](testLayers.live.testLayer)
