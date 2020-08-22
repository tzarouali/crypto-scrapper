package cryptoscrapper.services

import cats.effect.Sync
import cryptoscrapper.utils.ApplicationLogger
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

trait BaseService extends ApplicationLogger {

  protected val logger = new Log

  implicit def jsonDecoder[
    F[_]: Sync,
    A: Decoder
  ]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def jsonEncoder[
    F[_]: Sync,
    A: Encoder
  ]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

}
