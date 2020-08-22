package repositories

import cats.data.Kleisli
import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import cryptoscrapper.model.jooq.tables.Coin.COIN
import cryptoscrapper.model.{CoinDetails, ScrappedCoinDetails}
import cryptoscrapper.newtypes.{CoinId, CoinName, CoinSymbol, TraceId}
import cryptoscrapper.repositories.CoinRepository
import org.jooq.DSLContext

import scala.jdk.CollectionConverters._

class H2CoinRepository[F[_]: Sync: ContextShift](blocker: Blocker) extends CoinRepository[F, DSLContext] {

  override def create(coin: ScrappedCoinDetails)(implicit tid: TraceId): Kleisli[F, DSLContext, CoinId] = Kleisli {
    sql =>
      logger.debug("Before inserting coin")
      for {
        id <- blocker
          .delay {
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
              .execute()

            sql
              .select(COIN.ID)
              .from(COIN)
              .where(COIN.SYMBOL.eq(coin.symbol.value))
              .fetchOne()
              .map(r => CoinId(r.get(COIN.ID, classOf[Int])))
          }
        _ = logger.debug("After inserting coin")
      } yield id
  }

  override def findAll()(implicit tid: TraceId): Kleisli[F, DSLContext, List[CoinDetails]] = Kleisli { sql =>
    logger.debug("Before retrieving all coins")
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
      _ = logger.debug("After retrieving all coins")
    } yield coinDetails
  }

  override def findById(coinIds: List[CoinId])(implicit tid: TraceId): Kleisli[F, DSLContext, List[CoinDetails]] =
    Kleisli { sql =>
      logger.debug(s"Before retrieving all coins for IDs $coinIds")
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
        _ = logger.debug(s"After retrieving all coins for IDs $coinIds")
      } yield coinDetails
    }

}
