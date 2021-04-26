package me.daniyar.nomad

import me.daniyar.nomad.config.{DatabaseConfig, HttpConfig, NomadConfig}
import me.daniyar.nomad.repository.{JwtRepository, UserRepository}
import me.daniyar.nomad.util.Transactor
import zio.ZLayer
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.random._

object layers:
  type Layer0Env =
    Blocking with Clock with Random with Logging with NomadConfig

  type Layer1Env =
    Layer0Env with HttpConfig with DatabaseConfig

  type Layer2Env =
    Layer1Env with Transactor

  type Layer3Env =
    Layer2Env with UserRepository with JwtRepository

  type MainEnv = Layer3Env

  object live:
    val layer0: ZLayer[Blocking, Throwable, Layer0Env] =
      Blocking.any ++ Clock.live ++ Random.live ++
        Slf4jLogger.make((_, msg) => msg) ++ NomadConfig.live

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      ZLayer.identity[Layer0Env] ++ HttpConfig.fromNomadConfig ++ DatabaseConfig.fromNomadConfig

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      ZLayer.identity[Layer1Env] ++ Transactor.fromDatabaseConfig

    val layer3: ZLayer[Layer2Env, Throwable, Layer3Env] =
      ZLayer.identity[Layer2Env] ++ UserRepository.layer ++ JwtRepository.layer

    val appLayer: ZLayer[Blocking, Throwable, MainEnv] =
      layer0 >>> layer1 >>> layer2 >>> layer3
