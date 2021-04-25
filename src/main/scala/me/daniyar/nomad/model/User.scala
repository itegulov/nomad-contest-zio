package me.daniyar.nomad.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

final case class UserId(value: Long)

object UserId:
  implicit val decoder: Decoder[UserId] = Decoder.decodeLong.map[UserId](UserId.apply)
  implicit val encoder: Encoder[UserId] = Encoder.encodeLong.contramap[UserId](_.value)

final case class User(
  id: Option[UserId],
  firstName: String,
  lastName: String,
  email: String,
  password: String
)

object User:
  implicit val decoder: Decoder[User] =
    Decoder.forProduct4[User, Option[UserId], String, String, String](
      "id",
      "firstName",
      "lastName",
      "email"
    ) { case (id, firstName, lastName, email) =>
      User(id, firstName, lastName, email, "")
    }
  implicit val encoder: Encoder[User] =
    Encoder.forProduct4("id", "firstName", "lastName", "email")(u =>
      (u.id, u.firstName, u.lastName, u.email)
    )

final case class UserLoginInfo(
  email: String,
  password: String
)

object UserLoginInfo:
  implicit val decoder: Decoder[UserLoginInfo] = deriveDecoder[UserLoginInfo]
  implicit val encoder: Encoder[UserLoginInfo] = deriveEncoder[UserLoginInfo]

final case class UserSignupInfo(
  email: String,
  firstName: String,
  lastName: String,
  password: String
):
  def asUser: User =
    User(None, firstName, lastName, email, password)

object UserSignupInfo:
  implicit val decoder: Decoder[UserSignupInfo] = deriveDecoder[UserSignupInfo]
  implicit val encoder: Encoder[UserSignupInfo] = deriveEncoder[UserSignupInfo]
