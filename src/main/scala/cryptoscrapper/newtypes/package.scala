package cryptoscrapper

import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype

import scala.util.Try

package object newtypes {

  implicit def coercibleDecoder[A, B](
    implicit c: Coercible[Decoder[B], Decoder[A]],
    db: Decoder[B]
  ): Decoder[A] = c(db)

  implicit def coercibleEncoder[A, B](
    implicit c: Coercible[Encoder[B], Encoder[A]],
    eb: Encoder[B]
  ): Encoder[A] = c(eb)

  @newtype final case class BaseScrappingUri(value: String)
  @newtype final case class ApplicationHost(value: String)
  @newtype final case class ApplicationPort(value: Int)
  @newtype final case class DBUrl(value: String)
  @newtype final case class DBUser(value: String)
  @newtype final case class DBPassword(value: String)
  @newtype final case class DBPoolMinConnections(value: Int)
  @newtype final case class DBPoolMaxConnections(value: Int)
  @newtype final case class LeakConnectionDetectionMillis(value: Long)
  @newtype final case class NumberParallelHttpRequests(value: Int)
  @newtype final case class SecondsBetweenRequests(value: Int)
  @newtype final case class CoinId(value: Int)
  @newtype final case class CoinSymbol(value: String)
  @newtype final case class CoinName(value: String)
  @newtype final case class CoinRank(value: Int)
  @newtype final case class CoinPriceUSD(value: BigDecimal)
  @newtype final case class NumberThreadsIoPool(value: Int)
  @newtype final case class NumberThreadsDbPool(value: Int)
  @newtype final case class TracingHeaderName(value: String)
  @newtype final case class TraceId(value: String)
  object TraceId {
    def mk: TraceId = TraceId(UUID.randomUUID().toString)
  }
  @newtype final case class RabbitHost(value: String)
  @newtype final case class RabbitPort(value: Int)
  @newtype final case class RabbitUser(value: String)
  @newtype final case class RabbitPass(value: String)
  @newtype final case class ScappingServiceTimeoutMillis(value: Int)
  @newtype final case class CoinIds(value: String) {
    def toCoinIdList: Try[List[CoinId]] = Try(value.split(",").toList.map(c => CoinId(c.trim.toInt)))
  }
}
