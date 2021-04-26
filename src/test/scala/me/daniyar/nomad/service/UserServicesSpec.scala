package me.daniyar.nomad.service

import io.circe._
import me.daniyar.nomad.repository.UserRepository
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

object UserServicesSpec extends DefaultRunnableSpec:
  type UserTask[A] = RIO[UserRepository, A]

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
      testM("should not allow signing up twice") {
        val signUpReq = request[UserTask](Method.POST, "/signup").withEntity(testUserJson)
        val zio       = for {
          _        <- app.run(signUpReq)
          response <- app.run(signUpReq)
        } yield response

        checkRequestEquals[UserRepository, String](
          zio,
          Status.BadRequest,
          s"Email ${testUserEmail} is taken"
        )
      }
    ).provideSomeLayer[ZEnv](testLayers.live.testLayer)
