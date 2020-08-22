package cryptoscrapper.repositories

import cats.data.Kleisli
import cryptoscrapper.model.{CoinExtraDetails, ScrappedCoinDetails}
import cryptoscrapper.newtypes._

trait CoinDetailsRepository[F[_], E] extends BaseRepository {

  def create(coinId: CoinId, coin: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, E, Unit]

  def findById(coinId: CoinId)(implicit tid: TraceId): Kleisli[F, E, List[CoinExtraDetails]]

}
