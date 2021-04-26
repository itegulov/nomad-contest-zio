package me.daniyar.nomad.model

import java.time.Instant
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

final case class JwtId(value: String)

object JwtId:
  implicit val decoder: Decoder[JwtId] = Decoder.decodeString.map[JwtId](JwtId.apply)
  implicit val encoder: Encoder[JwtId] = Encoder.encodeString.contramap[JwtId](_.value)

final case class AugmentedJwt(
  id: JwtId,
  userId: UserId,
  jwt: String,
  expiry: Instant,
  lastTouched: Option[Instant]
)

object AugmentedJwt:
  implicit val decoder: Decoder[AugmentedJwt] =
    Decoder.forProduct5[AugmentedJwt, JwtId, UserId, String, Instant, Option[Instant]](
      "id",
      "userId",
      "jwt",
      "expiry",
      "lastTouched"
    ) { case (id, userId, jwt, expiry, lastTouched) =>
      AugmentedJwt(id, userId, jwt, expiry, lastTouched)
    }
  implicit val encoder: Encoder[AugmentedJwt] =
    Encoder.forProduct5("id", "userId", "jwt", "expiry", "lastTouched")(augmentedJwt =>
      (
        augmentedJwt.id,
        augmentedJwt.userId,
        augmentedJwt.jwt,
        augmentedJwt.expiry,
        augmentedJwt.lastTouched
      )
    )
