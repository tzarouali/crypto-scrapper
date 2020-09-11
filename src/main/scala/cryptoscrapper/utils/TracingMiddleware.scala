package cryptoscrapper.utils

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import cryptoscrapper.newtypes._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, HttpApp, Request}

class TracingMiddleware[F[_]: Sync](tracingHeaderName: TracingHeaderName) extends ApplicationLogger {

  final private val logger = new Log

  def middleware(
    http: HttpApp[F],
    logRequest: Boolean = false,
    logResponse: Boolean = false
  ): HttpApp[F] =
    Kleisli { req =>
      val createId: F[(Request[F], TraceId)] =
        for {
          id <- TraceId.mk.pure[F]
          tr <- Sync[F].delay(req.putHeaders(Header(Constants.TRACING_HEADER_NAMER, id.toString)))
        } yield (tr, id)

      for {
        mi       <- getTraceId(req)
        (tr, id) <- mi.fold(createId)(id => (req, id).pure[F])
        _ = if (logRequest) logger.info(s"$req")(id) else ()
        rs <- http(tr).map(_.putHeaders(Header(tracingHeaderName.value, id.value)))
        _ = if (logResponse) logger.info(s"$rs")(id) else ()
      } yield rs
    }

  def getTraceId(request: Request[F])(implicit F: Applicative[F]): F[Option[TraceId]] =
    F.pure(request.headers.get(CaseInsensitiveString(tracingHeaderName.value)).map(h => TraceId(h.value)))

}
