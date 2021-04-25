package me.daniyar.nomad.error

import scala.concurrent.duration.Duration

final case class DbException(cause: Throwable) extends Exception(cause)
