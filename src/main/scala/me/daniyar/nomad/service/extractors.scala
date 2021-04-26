package me.daniyar.nomad.service

import me.daniyar.nomad.model.UserId

import scala.util.Try

object UserIdVar:
  def unapply(str: String): Option[UserId] =
    str.toLongOption.map(UserId.apply)
