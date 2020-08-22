import cats.effect.{Blocker, IO}
import cryptoscrapper.controllers.CoinScrapperController
import cryptoscrapper.model.CoinDetails
import cryptoscrapper.model.jooq.Tables
import cryptoscrapper.model.provider.CoinLoreCoinDetails
import cryptoscrapper.newtypes._
import cryptoscrapper.repositories.impl.PostgresCoinDetailsRepository
import cryptoscrapper.services.HttpClient
import cryptoscrapper.services.impl.CoinService
import org.http4s.Method.GET
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Status, Uri}
import org.jooq.DSLContext
import org.scalamock.scalatest.MockFactory
import repositories.H2CoinRepository

import scala.concurrent.ExecutionContext.global
import scala.jdk.CollectionConverters._

class CoinScrapperControllerSpec extends BaseTestSpec with MockFactory {

  "test 1" should {
    "work" in {
      val ds             = mkDataSource
      val httpClientStub = stub[HttpClient[IO]]
      (httpClientStub.expect(_: Uri)(_: EntityDecoder[IO, _], _: TraceId)).when(*, *, *).returns(IO.unit)
      val coinRepo            = new H2CoinRepository[IO](Blocker.liftExecutionContext(global))
      val coinDetailsRepo     = new PostgresCoinDetailsRepository[IO](Blocker.liftExecutionContext(global))
      val coinService         = new CoinService[IO, DSLContext](coinRepo, coinDetailsRepo)
      val coinScrapperController = new CoinScrapperController[IO, DSLContext](
        coinService,
        mkHikariTransactor(ds)
      )
      val httpApp = coinScrapperController.router.orNotFound
      withInitialData(ds, getClass.getClassLoader.getResource("CoinScrapperControllerSpec-01.sql").toURI) { _ =>
        val execution = httpApp.run(GET(Uri.unsafeFromString("/api/wrong")).unsafeRunSync()).unsafeRunSync()
        execution.status mustBe Status.NotFound
      }
    }
  }

  "test 2" should {
    "work" in {
      val ds             = mkDataSource
      val httpClientStub = stub[HttpClient[IO]]
      val c = CoinLoreCoinDetails(
        id = CoinId(1),
        symbol = CoinSymbol("LTC"),
        name = CoinName("Litecoin"),
        rank = CoinRank(8),
        price_usd = CoinPriceUSD(BigDecimal(10.32))
      )
      (httpClientStub.expect(_: Uri)(_: EntityDecoder[IO, _], _: TraceId)).when(*, *, *).returns(IO.pure(List(c)))
      val coinRepo            = new H2CoinRepository[IO](Blocker.liftExecutionContext(global))
      val coinDetailsRepo     = new PostgresCoinDetailsRepository[IO](Blocker.liftExecutionContext(global))
      val coinService         = new CoinService[IO, DSLContext](coinRepo, coinDetailsRepo)
      val coinScrapperController = new CoinScrapperController[IO, DSLContext](
        coinService,
        mkHikariTransactor(ds)
      )
      val httpApp = coinScrapperController.router.orNotFound

      withInitialData(ds, getClass.getClassLoader.getResource("CoinScrapperControllerSpec-01.sql").toURI) { sql =>
        val execution = httpApp.run(GET(Uri.unsafeFromString("/api/coins?id=1")).unsafeRunSync()).unsafeRunSync()
        val result    = execution.asJson.unsafeRunSync().as[List[CoinDetails]]
        result.value mustBe List(
          CoinDetails(
            id = CoinId(1),
            symbol = CoinSymbol("test-symbol1"),
            name = CoinName("test-name1"),
            extraDetails = List()
          )
        )

        val coins = sql.selectFrom(Tables.COIN).orderBy(Tables.COIN.ID).fetch().asScala.toList
        coins.size mustBe 2
        coins.head.getSymbol mustBe "test-symbol1"
        coins.head.getName mustBe "test-name1"
        coins.last.getSymbol mustBe "test-symbol2"
        coins.last.getName mustBe "test-name2"

        val coinDetails = sql.selectFrom(Tables.COIN_DETAILS).fetch().asScala.toList
        coinDetails.size mustBe 0
      }
    }
  }

}
