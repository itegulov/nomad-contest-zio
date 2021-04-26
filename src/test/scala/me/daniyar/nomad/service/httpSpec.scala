package me.daniyar.nomad.service

import org.http4s.{EntityDecoder, Method, Request, Response, Status, Uri}
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._

def request[F[_]](
  method: Method,
  uri: String
): Request[F] = Request(method = method, uri = Uri.fromString(uri).toOption.get)

def checkRequest[R, A](
  actualR: RIO[R, Response[RIO[R, *]]],
  expectedStatus: Status,
  bodyAssertion: AssertionM[A]
)(implicit
  ev: EntityDecoder[RIO[R, *], A]
): RIO[R, TestResult] =
  for {
    actual      <- actualR
    bodyResult  <- assertM(actual.as[A])(bodyAssertion)
    statusResult = assert(actual.status)(equalTo(expectedStatus))
  } yield bodyResult && statusResult

def checkRequestEquals[R, A](
  actual: RIO[R, Response[RIO[R, *]]],
  expectedStatus: Status,
  expectedBody: A
)(implicit
  ev: EntityDecoder[RIO[R, *], A]
): RIO[R, TestResult] = checkRequest(actual, expectedStatus, equalTo(expectedBody))
