package cryptoscrapper.tasks

import cats.Parallel
import cats.data.Kleisli
import cats.effect.{ContextShift, Sync, Timer}
import cats.implicits._
import cron4s.expr.CronExpr
import cryptoscrapper.model.rabbit.{RabbitExchangeAndQueueNames, RabbitMessageHeaders, RabbitMessageTypes}
import cryptoscrapper.newtypes._
import cryptoscrapper.services.impl.CoinScrapperService
import cryptoscrapper.utils.Constants
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.StringVal
import dev.profunktor.fs2rabbit.model.{AmqpMessage, AmqpProperties, ExchangeName, RoutingKey}
import eu.timepit.fs2cron.awakeEveryCron
import fs2.Stream
import io.circe.syntax._

class CoinScrapperJob[F[_]: Sync: ContextShift: Timer: Parallel](
  cronExpression: CronExpr,
  coinScrapperService: CoinScrapperService[F],
  rabbitClient: RabbitClient[F],
  coinIds: List[CoinId]
) extends BackgroundJob[F] {

  private val exchangeName = ExchangeName(RabbitExchangeAndQueueNames.CoinScrap.EXCHANGE_NAME)
  private val routingKey   = RoutingKey(RabbitExchangeAndQueueNames.CoinScrap.ROUTING_KEY)
  implicit val stringMessageEncoder: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
    Kleisli(s => s.copy(payload = s.payload.getBytes(Constants.UTF_8_ENCODING)).pure[F])

  override def run: F[Unit] =
    rabbitClient.createConnectionChannel.use { implicit chan =>
      for {
        pub <- rabbitClient.createPublisher[AmqpMessage[String]](exchangeName, routingKey)
        _ <- awakeEveryCron[F](cronExpression)
          .flatMap { _ =>
            implicit val tid: TraceId = TraceId.mk
            Stream.eval {
              logger.debug("Running scrapping job")
              for {
                scrappedCoins <- coinScrapperService.scrapCoinDetails(coinIds)
                _ = logger.debug("Scrapping job finished. About to send message with scrapped coins")
                _ <- pub.apply(
                  AmqpMessage[String](
                    payload = scrappedCoins.asJson.noSpaces,
                    properties = AmqpProperties(headers =
                      Map(
                        RabbitMessageHeaders.MESSAGE_TYPE -> StringVal(RabbitMessageTypes.SCRAPPED_COINS_MESSAGE),
                        RabbitMessageHeaders.TRACE_ID -> StringVal(tid.value),
                      )
                    )
                  )
                )
                _ = logger.debug("Message successfully sent")
              } yield ()
            }.handleErrorWith(_ => Stream.empty)
          }
          .compile
          .drain
      } yield ()
    }

}
