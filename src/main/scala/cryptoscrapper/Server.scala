package cryptoscrapper

import cats.{MonadError, Parallel}
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import com.zaxxer.hikari.HikariDataSource
import cryptoscrapper.controllers.CoinScrapperController
import cryptoscrapper.model.{AppConfig, ServerConfig}
import cryptoscrapper.newtypes._
import cryptoscrapper.pools.ThreadPools
import cryptoscrapper.queues.CoinQueueConsumerService
import cryptoscrapper.repositories.impl.{PostgresCoinDetailsRepository, PostgresCoinRepository}
import cryptoscrapper.services.impl.{CoinScrapperService, CoinService, Http4sHttpClient}
import cryptoscrapper.tasks.CoinScrapperJob
import cryptoscrapper.utils.{Constants, HikariConfigMaker, RabbitConfigMaker, TracingMiddleware}
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import io.circe.config.parser
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.jooq.DSLContext

import scala.concurrent.ExecutionContext.global
import scala.util.Try

object Server extends IOApp {
  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer: Parallel]: Resource[F, ServerConfig[F]] = {
    val ME = implicitly[MonadError[F, Throwable]]
    for {
      appConfig <- Resource.liftF(parser.decodeF[F, AppConfig]())
      coinIdsToScrap <- Resource.liftF(
        ME.fromTry(Try(appConfig.scrapCoinsJob.coinIds.split(",").toList.map(c => CoinId(c.trim.toInt))))
      )
      pools = new ThreadPools[F](appConfig)
      blockingEC      <- pools.dbPool
      rabbitEC        <- pools.rabbitPool
      hikariConfig    <- Resource.liftF(Sync[F].delay(HikariConfigMaker.makeHikariConfig(appConfig)))
      ds              <- Resource.liftF(Sync[F].delay(new HikariDataSource(hikariConfig)))
      blazehttpClient <- BlazeClientBuilder[F](global).resource
      httpClient             = new Http4sHttpClient[F](blazehttpClient)
      dbTransactor           = new HikariTransactor[F](ds)
      coinRepo               = new PostgresCoinRepository[F](Blocker.liftExecutionContext(blockingEC))
      coinDetailsRepo        = new PostgresCoinDetailsRepository[F](Blocker.liftExecutionContext(blockingEC))
      coinService            = new CoinService[F, DSLContext](coinRepo, coinDetailsRepo)
      coinScrapperService    = new CoinScrapperService[F](appConfig, httpClient)
      coinScrapperController = new CoinScrapperController[F, DSLContext](coinService, dbTransactor)
      httpApp = new TracingMiddleware[F](TracingHeaderName(Constants.TRACING_HEADER_NAMER))
        .middleware(coinScrapperController.router.orNotFound)
      rabbitClient <- Resource.liftF(
        RabbitClient[F](RabbitConfigMaker.mk(appConfig), Blocker.liftExecutionContext(rabbitEC))
      )

      job = new CoinScrapperJob[F](
        appConfig.scrapCoinsJob.cronExpression,
        coinScrapperService,
        rabbitClient,
        coinIdsToScrap
      )

      coinQueueService = new CoinQueueConsumerService[F, DSLContext](
        rabbitClient,
        coinService,
        dbTransactor
      )

      server <- BlazeServerBuilder[F](global)
        .bindHttp(appConfig.server.port.value)
        .withHttpApp(httpApp)
        .resource
    } yield ServerConfig(server = server, jobs = List(job), queues = List(coinQueueService))
  }

  def run(args: List[String]): IO[ExitCode] =
    createServer
      .use { serverConfig =>
        serverConfig.jobs.foreach(_.run.unsafeRunAsyncAndForget())
        serverConfig.queues.foreach(_.run.unsafeRunAsyncAndForget())
        IO.never
      }
      .as(ExitCode.Success)

}
