package cryptoscrapper.repositories

import cats.data.Kleisli
import cryptoscrapper.model.{CoinDetails, ScrappedCoinDetails}
import cryptoscrapper.newtypes._

trait CoinRepository[F[_], E] extends BaseRepository {

  def create(coin: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, E, CoinId]

  def findAll()(implicit tid: TraceId): Kleisli[F, E, List[CoinDetails]]

  def findById(coinIds: List[CoinId])(implicit tid: TraceId): Kleisli[F, E, List[CoinDetails]]

}
