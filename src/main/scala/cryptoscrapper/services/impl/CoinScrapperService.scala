package cryptoscrapper.services.impl

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import cats.{MonadError, Parallel}
import cryptoscrapper.model.ApplicationError.{BusinessError, UnexpectedError}
import cryptoscrapper.model.provider.CoinLoreCoinDetails
import cryptoscrapper.model.{AppConfig, ScrappedCoinDetails}
import cryptoscrapper.newtypes._
import cryptoscrapper.services.{BaseService, HttpClient}
import fs2._
import org.http4s.Uri

import scala.concurrent.duration._

class CoinScrapperService[F[_]: Concurrent: Parallel: Timer](appConfig: AppConfig, httpClient: HttpClient[F])
    extends BaseService {

  final private val me = implicitly[MonadError[F, Throwable]]

  def scrapCoinDetails(coinIds: List[CoinId])(implicit tid: TraceId): F[List[ScrappedCoinDetails]] = {
    val coinApiServiceTimeout = appConfig.server.scappingServiceTimeoutMillis.value.millis
    val waitTimeBetweenReqs   = appConfig.server.secondsBetweenRequests.value.seconds
    val numberParallelReqs    = appConfig.server.numberParallelHttpRequests.value
    Stream
      .emits(coinIds)
      .chunkN(numberParallelReqs)
      .covary[F]
      .metered(waitTimeBetweenReqs)
      .evalMap { ids =>
        ids.toList.parTraverse { id =>
          val coinUri = appConfig.baseScrappingUri.value + id
          for {
            uri <- me.fromEither(
              Uri.fromString(coinUri).leftMap(e => BusinessError(s"Error creating URI ${e.message}"))
            )
            _ = logger.debug(s"Executing GET request to URI $coinUri")
            details <- httpClient
              .expect[List[CoinLoreCoinDetails]](uri)
              .runTimed(coinApiServiceTimeout, s"Timeout retrieving coin details. Waited $coinApiServiceTimeout")
              .recoverWith {
                case e =>
                  logger.error(e.getMessage)
                  me.raiseError(UnexpectedError(s"Error retrieving coin details: ${e.getMessage}"))
              }
            d <- me.fromOption(details.headOption, BusinessError(s"No coin details returned from URI $coinUri"))
          } yield ScrappedCoinDetails(
            id = d.id,
            symbol = d.symbol,
            name = d.name,
            rank = d.rank,
            priceUsd = d.price_usd
          )
        }
      }
      .compile
      .toList
      .map(_.flatten)
  }

}
