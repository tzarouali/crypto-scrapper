package cryptoscrapper.services.impl

import cryptoscrapper.newtypes.TraceId
import cryptoscrapper.services.{BaseService, HttpClient}
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}

class Http4sHttpClient[F[_]](httpClient: Client[F]) extends HttpClient[F] with BaseService {

  override def expect[A](uri: Uri)(implicit d: EntityDecoder[F, A], tid: TraceId): F[A] = httpClient.expect[A](uri)

}
