package cryptoscrapper.queues

import cats.effect._
import cats.implicits._
import cats.{MonadError, Parallel}
import cryptoscrapper.DbTransactor
import cryptoscrapper.model.ScrappedCoinDetails
import cryptoscrapper.model.rabbit.{RabbitExchangeAndQueueNames, RabbitMessageHeaders, RabbitMessageTypes}
import cryptoscrapper.newtypes.TraceId
import cryptoscrapper.services.impl.CoinService
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model._
import fs2.Stream
import io.circe.{parser => CirceParser}

class CoinQueueConsumerService[F[_]: Sync: Parallel, E](
  client: RabbitClient[F],
  coinService: CoinService[F, E],
  tx: DbTransactor[F, E]
) extends QueueService[F] {

  final private val ME = implicitly[MonadError[F, Throwable]]

  private val exchangeName = ExchangeName(RabbitExchangeAndQueueNames.CoinScrap.EXCHANGE_NAME)
  private val queueName    = QueueName(RabbitExchangeAndQueueNames.CoinScrap.QUEUE_NAME)
  private val routingKey   = RoutingKey(RabbitExchangeAndQueueNames.CoinScrap.ROUTING_KEY)

  private def parseExtraCoins(rawMessage: AmqpEnvelope[String])(implicit tid: TraceId): F[Unit] =
    for {
      scrappedCoins <- ME.fromEither(CirceParser.decode[List[ScrappedCoinDetails]](rawMessage.payload))
      _             <- tx.transact(scrappedCoins.parTraverse(coinService.createCoin))
    } yield ()

  private def extractTraceIdHeader(headers: Map[String, AmqpFieldValue]): TraceId =
    headers
      .get(RabbitMessageHeaders.TRACE_ID)
      .map(tid => TraceId(tid.toValueWriterCompatibleJava.toString))
      .getOrElse(TraceId.mk)

  class Flow(consumer: Stream[F, AmqpEnvelope[String]], acker: AckResult => F[Unit]) {
    val flow: Stream[F, Unit] = consumer
      .evalMap { msg =>
        implicit val tid: TraceId = extractTraceIdHeader(msg.properties.headers)
        msg.properties.headers.get(RabbitMessageHeaders.MESSAGE_TYPE) match {
          case Some(h) if h.toValueWriterCompatibleJava == RabbitMessageTypes.SCRAPPED_COINS_MESSAGE =>
            logger.debug(
              s"Received message of type ${RabbitMessageTypes.SCRAPPED_COINS_MESSAGE}. Raw message is: ${msg.payload}"
            )
            ME.attempt(parseExtraCoins(msg)).map {
              case Right(_) =>
                logger.debug(s"Successfully handled the message ${RabbitMessageTypes.SCRAPPED_COINS_MESSAGE}")
                AckResult.Ack(msg.deliveryTag)
              case Left(_) =>
                AckResult.NAck(msg.deliveryTag)
            }
          case Some(h) =>
            logger.warn(s"Unrecognized message. Type is: ${h.toValueWriterCompatibleJava}, payload is: ${msg.payload}")
            AckResult.NAck(msg.deliveryTag).pure[F].widen[AckResult]
          case None =>
            logger.warn(s"Unrecognized message ${msg.payload}")
            AckResult.NAck(msg.deliveryTag).pure[F].widen[AckResult]
        }
      }
      .evalMap(acker)
  }

  override def run: F[Unit] = client.createConnectionChannel.use { implicit channel =>
    for {
      _                 <- client.declareExchange(exchangeName, ExchangeType.Topic)
      _                 <- client.declareQueue(DeclarationQueueConfig.default(queueName))
      _                 <- client.bindQueue(queueName, exchangeName, routingKey)
      (acker, consumer) <- client.createAckerConsumer[String](queueName)
      result = new Flow(consumer, acker).flow
      _ <- result.compile.drain
    } yield ()
  }

}
