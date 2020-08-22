package cryptoscrapper.controllers

import cats.effect.Sync
import cryptoscrapper.newtypes._
import cryptoscrapper.utils.{ApplicationLogger, Constants}
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, EntityEncoder, Request}

trait BaseController extends ApplicationLogger {

  protected val logger = new Log

  implicit def jsonDecoder[
    F[_]: Sync,
    A: Decoder
  ]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def jsonEncoder[
    F[_]: Sync,
    A: Encoder
  ]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  protected def extractTraceId[F[_]](req: Request[F]): TraceId =
    req.headers
      .get(CaseInsensitiveString(Constants.TRACING_HEADER_NAMER))
      .map(h => TraceId(h.value))
      .getOrElse(TraceId.mk)

}
