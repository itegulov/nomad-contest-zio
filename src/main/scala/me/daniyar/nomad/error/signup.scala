package me.daniyar.nomad.error

sealed class SignupException(cause: Throwable)        extends Exception(cause):
  def this() = this(null)
final case class EmailIsTakenException(email: String) extends SignupException
