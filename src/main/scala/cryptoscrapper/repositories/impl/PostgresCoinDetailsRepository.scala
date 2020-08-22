package cryptoscrapper.repositories.impl

import java.time.OffsetDateTime

import cats.data.Kleisli
import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import cryptoscrapper.model.jooq.tables.CoinDetails.COIN_DETAILS
import cryptoscrapper.model.{CoinExtraDetails, ScrappedCoinDetails}
import cryptoscrapper.newtypes._
import cryptoscrapper.repositories.CoinDetailsRepository
import org.jooq.DSLContext

import scala.jdk.CollectionConverters._

class PostgresCoinDetailsRepository[F[_]: Sync: ContextShift](blocker: Blocker)
    extends CoinDetailsRepository[F, DSLContext] {

  override def create(coinId: CoinId, coin: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, DSLContext, Unit] =
    Kleisli { sql =>
      logger.debug("[PostgresCoinDetailsRepository] Before inserting coin details")
      for {
        _ <- blocker
          .delay(
            sql
              .insertInto(
                COIN_DETAILS,
                COIN_DETAILS.COIN_ID,
                COIN_DETAILS.RANK,
                COIN_DETAILS.PRICE_USD,
                COIN_DETAILS.CREATED
              )
              .values(
                coinId.value,
                coin.rank.value.asJava,
                coin.priceUsd.value.asJava,
                OffsetDateTime.now()
              )
              .onDuplicateKeyIgnore()
              .execute()
          )
        _ = logger.debug("[PostgresCoinDetailsRepository] After inserting coin details")
      } yield ()
    }

  override def findById(coinId: CoinId)(implicit tid: TraceId): Kleisli[F, DSLContext, List[CoinExtraDetails]] =
    Kleisli { sql =>
      logger.debug(s"[PostgresCoinDetailsRepository] Before retrieving all coin extra details for ID $coinId")
      for {
        extraDetails <- blocker
          .delay(
            sql
              .select(
                COIN_DETAILS.RANK,
                COIN_DETAILS.PRICE_USD
              )
              .from(COIN_DETAILS)
              .where(COIN_DETAILS.COIN_ID.eq(coinId.value))
              .fetch()
              .asScala
              .map(record =>
                CoinExtraDetails(
                  rank = CoinRank(record.get(COIN_DETAILS.RANK, classOf[Int])),
                  priceUsd = CoinPriceUSD(BigDecimal(record.get(COIN_DETAILS.PRICE_USD, classOf[java.math.BigDecimal])))
                )
              )
              .toList
          )
        _ = logger.debug(s"[PostgresCoinDetailsRepository] After retrieving all coin extra details for ID $coinId")
      } yield extraDetails
    }

}
