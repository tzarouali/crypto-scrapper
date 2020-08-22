package cryptoscrapper.repositories.impl

import cats.data.Kleisli
import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import cryptoscrapper.model.jooq.tables.Coin.COIN
import cryptoscrapper.model.{CoinDetails, ScrappedCoinDetails}
import cryptoscrapper.newtypes._
import cryptoscrapper.repositories.CoinRepository
import org.jooq.DSLContext
import org.jooq.scalaextensions.Conversions._

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

class PostgresCoinRepository[F[_]: Sync: ContextShift](blocker: Blocker) extends CoinRepository[F, DSLContext] {

  override def create(coin: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, DSLContext, CoinId] = Kleisli {
    sql =>
      logger.debug("[PostgresCoinRepository] Before inserting coin")
      for {
        maybeCoinId <- blocker
          .delay(
            sql
              .insertInto(
                COIN,
                COIN.NAME,
                COIN.SYMBOL
              )
              .values(
                coin.name.value,
                coin.symbol.value
              )
              .onDuplicateKeyIgnore()
              .returningResult(COIN.ID)
              .fetchOptional()
              .toScala
              .map(record => CoinId(record.get(COIN.ID)))
          )
        id <- if (maybeCoinId.isEmpty)
          blocker.delay(
            sql
              .select(COIN.ID)
              .from(COIN)
              .where(COIN.SYMBOL.eq(coin.symbol.value))
              .fetchOneOption()
              .map(record => CoinId(record.get(COIN.ID)))
              .get
          )
        else maybeCoinId.get.pure[F]
        _ = logger.debug("[PostgresCoinRepository] After inserting coin")
      } yield id
  }

  override def findAll()(implicit tid: TraceId): Kleisli[F, DSLContext, List[CoinDetails]] = Kleisli { sql =>
    logger.debug("[PostgresCoinRepository] Before retrieving all coins")
    for {
      coinDetails <- blocker
        .delay(
          sql
            .select(
              COIN.ID,
              COIN.NAME,
              COIN.SYMBOL
            )
            .from(COIN)
            .orderBy(COIN.SYMBOL)
            .fetch()
            .asScala
            .map(record =>
              CoinDetails(
                id = CoinId(record.get(COIN.ID, classOf[Int])),
                symbol = CoinSymbol(record.get(COIN.SYMBOL, classOf[String])),
                name = CoinName(record.get(COIN.NAME, classOf[String])),
                extraDetails = List()
              )
            )
            .toList
        )
      _ = logger.debug("[PostgresCoinRepository] After retrieving all coins")
    } yield coinDetails
  }

  override def findById(coinIds: List[CoinId])(implicit tid: TraceId): Kleisli[F, DSLContext, List[CoinDetails]] =
    Kleisli { sql =>
      logger.debug(s"[PostgresCoinRepository] Before retrieving all coins for IDs $coinIds")
      for {
        coinDetails <- blocker
          .delay(
            sql
              .select(
                COIN.ID,
                COIN.NAME,
                COIN.SYMBOL
              )
              .from(COIN)
              .where(COIN.ID.in(coinIds.map(_.value).asJava))
              .fetch()
              .asScala
              .map(record =>
                CoinDetails(
                  id = CoinId(record.get(COIN.ID, classOf[Int])),
                  symbol = CoinSymbol(record.get(COIN.SYMBOL, classOf[String])),
                  name = CoinName(record.get(COIN.NAME, classOf[String])),
                  extraDetails = List()
                )
              )
              .toList
          )
        _ = logger.debug(s"[PostgresCoinRepository] After retrieving all coins for IDs $coinIds")
      } yield coinDetails
    }

}
