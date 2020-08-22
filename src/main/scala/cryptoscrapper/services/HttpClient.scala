package cryptoscrapper.services

import cryptoscrapper.newtypes.TraceId
import org.http4s.{EntityDecoder, Uri}

trait HttpClient[F[_]] {
  def expect[A](uri: Uri)(implicit d: EntityDecoder[F, A], tid: TraceId): F[A]
}