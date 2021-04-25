package me.daniyar.nomad

import cats.effect.ExitCode
import me.daniyar.nomad.config._
import me.daniyar.nomad.layers.MainEnv
import me.daniyar.nomad.repository.UserRepository
import me.daniyar.nomad.service._
import me.daniyar.nomad.util.Transactor
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import zio.{RIO, ZEnv, ZIO, ExitCode => ZExitCode}
import zio.clock.Clock
import zio.interop.catz._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends zio.App:
  type MainTask[A] = RIO[MainEnv, A]

  val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  def runHttp[R <: Clock](
    httpApp: HttpApp[RIO[R, *]],
    port: Int
  ): ZIO[R, Throwable, Unit] =
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder
        .apply[Task](rts.platform.executor.asEC)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(CORS(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }

  def run(args: List[String]) =
    val prog =
      for {
        config <- getNomadConfig
        httpApp = Router[MainTask]("/api/users" -> userServices).orNotFound
        _      <- runHttp(httpApp, config.http.port)
      } yield ZExitCode.success

    prog.provideSomeLayer[ZEnv](layers.live.appLayer).orDie
