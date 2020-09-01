package cryptoscrapper.services.impl

import cats.Parallel
import cats.implicits._
import cats.data.Kleisli
import cats.effect.Sync
import cryptoscrapper.newtypes._
import cryptoscrapper.model.{CoinDetails, ScrappedCoinDetails}
import cryptoscrapper.repositories.{CoinDetailsRepository, CoinRepository}
import cryptoscrapper.services.BaseService

class CoinService[F[_]: Sync: Parallel, E](
  coinRepo: CoinRepository[F, E],
  coinDetailsRepo: CoinDetailsRepository[F, E]
) extends BaseService {

  def createCoin(scrappedCoinDetails: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, E, Unit] =
    for {
      _      <- Kleisli.liftF[F, E, Unit](Sync[F].delay(logger.debug("Before storing coin & details")))
      coinId <- coinRepo.create(scrappedCoinDetails)
      _      <- coinDetailsRepo.create(coinId, scrappedCoinDetails)
      _ = logger.debug("After storing coin & details")
    } yield ()

  def findCoinWithDetails(coinIds: List[CoinId])(implicit tid: TraceId): Kleisli[F, E, List[CoinDetails]] =
    for {
      _           <- Kleisli.liftF[F, E, Unit](Sync[F].delay(logger.debug(s"Before retrieving coin & details for IDs $coinIds")))
      coinDetails <- if (coinIds.isEmpty) coinRepo.findAll() else coinRepo.findById(coinIds)
      r           <- coinDetails.parTraverse(cd => coinDetailsRepo.findById(cd.id).map(ed => cd.copy(extraDetails = ed)))
      _ = logger.debug(s"After retrieving coin & details for IDs $coinIds")
    } yield r

}
