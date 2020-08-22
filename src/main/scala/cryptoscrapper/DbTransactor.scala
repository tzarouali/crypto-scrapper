package cryptoscrapper

import cats.data.Kleisli
import cryptoscrapper.newtypes.TraceId

trait DbTransactor[F[_], E] {
  def transact[A](b: Kleisli[F, E, A])(implicit tid: TraceId): F[A]
}
