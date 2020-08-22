package cryptoscrapper.model.provider

import io.circe._
import io.circe.generic.semiauto._
import cryptoscrapper.newtypes.{CoinId, CoinName, CoinPriceUSD, CoinRank, CoinSymbol}

final case class CoinLoreCoinDetails(
  id: CoinId,
  symbol: CoinSymbol,
  name: CoinName,
  rank: CoinRank,
  price_usd: CoinPriceUSD
)

object CoinLoreCoinDetails {
  implicit val d: Decoder[CoinLoreCoinDetails] = deriveDecoder
}