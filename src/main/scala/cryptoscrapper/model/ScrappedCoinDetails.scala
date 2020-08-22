package cryptoscrapper.model

import io.circe._
import io.circe.generic.semiauto._
import cryptoscrapper.newtypes._

final case class ScrappedCoinDetails(
  id: CoinId,
  symbol: CoinSymbol,
  name: CoinName,
  rank: CoinRank,
  priceUsd: CoinPriceUSD
)

object ScrappedCoinDetails {
  implicit val e: Encoder[ScrappedCoinDetails] = deriveEncoder
  implicit val d: Decoder[ScrappedCoinDetails] = deriveDecoder
}
