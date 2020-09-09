package cryptoscrapper.services

import cats.effect.{Concurrent, Timer}
import cryptoscrapper.model.ApplicationError.TimeOutError

import scala.concurrent.duration.FiniteDuration

package object impl {

  implicit final class ConcurrentOps[F[_], A](val f: F[A]) extends AnyVal {
    def runTimed(duration: FiniteDuration, errorMsg: String)(implicit T: Timer[F], C: Concurrent[F]): F[A] =
      Concurrent.timeoutTo(f, duration, Concurrent[F].raiseError(TimeOutError(errorMsg)))
  }

}
