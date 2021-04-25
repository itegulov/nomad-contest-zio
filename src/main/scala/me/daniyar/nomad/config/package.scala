package me.daniyar.nomad

import zio.{Has, URIO, ZIO}

package object config:
  type NomadConfig    = Has[NomadConfig.Config]
  type HttpConfig     = Has[HttpConfig.Config]
  type DatabaseConfig = Has[DatabaseConfig.Config]

  val getNomadConfig: URIO[NomadConfig, NomadConfig.Config] =
    ZIO.access(_.get)

  val getHttpConfig: URIO[HttpConfig, HttpConfig.Config] =
    ZIO.access(_.get)

  val getDatabaseConfig: URIO[DatabaseConfig, DatabaseConfig.Config] =
    ZIO.access(_.get)
