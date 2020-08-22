package cryptoscrapper.model

import io.circe._
import io.circe.generic.semiauto._

abstract class ApplicationError(msg: String) extends Throwable(msg)

object ApplicationError {
  final case class ValidationError(msg: String)    extends ApplicationError(msg)
  object ValidationError {
    implicit val e: Encoder[ValidationError] = deriveEncoder
    implicit val d: Decoder[ValidationError] = deriveDecoder
  }
  final case class BusinessError(msg: String) extends ApplicationError(msg)
  object BusinessError {
    implicit val e: Encoder[BusinessError] = deriveEncoder
    implicit val d: Decoder[BusinessError] = deriveDecoder
  }
  final case class UnexpectedError(msg: String) extends ApplicationError(msg)
  object UnexpectedError {
    implicit val e: Encoder[UnexpectedError] = deriveEncoder
    implicit val d: Decoder[UnexpectedError] = deriveDecoder
  }
}
