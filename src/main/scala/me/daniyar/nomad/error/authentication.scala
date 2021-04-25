package me.daniyar.nomad.error

sealed class AuthenticationException(cause: Throwable)   extends Exception(cause):
  def this() = this(null)
final case class InvalidPasswordException(email: String) extends AuthenticationException
final case class UserNotFoundException(email: String)    extends AuthenticationException
