package cryptoscrapper.model

import io.circe._
import io.circe.generic.semiauto._
import cryptoscrapper.newtypes.{CoinId, CoinName, CoinPriceUSD, CoinRank, CoinSymbol}

final case class CoinDetails(id: CoinId, symbol: CoinSymbol, name: CoinName, extraDetails: List[CoinExtraDetails])
object CoinDetails {
  implicit val e: Encoder[CoinDetails] = deriveEncoder
  implicit val d: Decoder[CoinDetails] = deriveDecoder
}

final case class CoinExtraDetails(rank: CoinRank, priceUsd: CoinPriceUSD)
object CoinExtraDetails {
  implicit val e: Encoder[CoinExtraDetails] = deriveEncoder
  implicit val d: Decoder[CoinExtraDetails] = deriveDecoder
}
