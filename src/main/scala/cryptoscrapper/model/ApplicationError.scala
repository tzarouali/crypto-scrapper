package cryptoscrapper.model

import io.circe._
import io.circe.generic.semiauto._

abstract class ApplicationError(msg: String) extends Throwable(msg)

object ApplicationError {
  final case class ValidationError(msg: String)    extends ApplicationError(msg)
  object ValidationError {
    implicit val e: Encoder[ValidationError] = deriveEncoder
  }
  final case class BusinessError(msg: String) extends ApplicationError(msg)
  object BusinessError {
    implicit val e: Encoder[BusinessError] = deriveEncoder
  }
  final case class UnexpectedError(msg: String) extends ApplicationError(msg)
  object UnexpectedError {
    implicit val e: Encoder[UnexpectedError] = deriveEncoder
  }
  final case class TimeOutError(msg: String) extends ApplicationError(msg)
  object TimeOutError {
    implicit val e: Encoder[TimeOutError] = deriveEncoder
  }
}
