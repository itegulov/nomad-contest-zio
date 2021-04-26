package me.daniyar.nomad.service

import me.daniyar.nomad.config.DatabaseConfig
import me.daniyar.nomad.repository.{JwtRepository, UserRepository}
import me.daniyar.nomad.testing.{PostgresqlDocker, TestContainersDatabaseConfig}
import me.daniyar.nomad.util.Transactor
import zio.{ZEnv, ZLayer}
import zio.blocking.Blocking
import zio.random._

object testLayers:
  type Layer0Env =
    Blocking with Random with PostgresqlDocker

  type Layer1Env =
    Layer0Env with DatabaseConfig

  type Layer2Env =
    Layer1Env with Transactor

  type Layer3Env =
    Random with UserRepository with JwtRepository

  type TestEnv = Layer3Env

  object live:
    val layer0: ZLayer[Blocking, Nothing, Layer0Env] =
      Blocking.any ++ Random.live ++ PostgresqlDocker.live

    val layer1: ZLayer[Layer0Env, Nothing, Layer1Env] =
      ZLayer.identity ++ TestContainersDatabaseConfig.test

    val layer2: ZLayer[Layer1Env, Nothing, Layer2Env] =
      ZLayer.identity[Layer1Env] ++ Transactor.fromDatabaseConfig.orDie

    val layer3: ZLayer[Layer2Env, Nothing, Layer3Env] =
      Random.any ++ UserRepository.layer ++ JwtRepository.layer

    val testLayer: ZLayer[Blocking, Nothing, TestEnv] =
      layer0 >>> layer1 >>> layer2 >>> layer3
