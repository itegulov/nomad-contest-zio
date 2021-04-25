package me.daniyar.nomad.config

import pureconfig._
import zio.ZLayer

object HttpConfig:
  final case class Config(port: Int, baseUrl: String)

  object Config:
    implicit val reader: ConfigReader[Config] =
      ConfigReader.forProduct2("port", "baseUrl")(Config.apply)
    implicit val writer: ConfigWriter[Config] =
      ConfigWriter.forProduct2("port", "baseUrl")(c => (c.port, c.baseUrl))

  val fromNomadConfig: ZLayer[NomadConfig, Nothing, HttpConfig] =
    ZLayer.fromService(_.http)
