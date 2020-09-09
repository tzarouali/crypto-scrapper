package cryptoscrapper.model

import cron4s.circe._
import cron4s.expr.CronExpr
import cryptoscrapper.newtypes._
import io.circe._
import io.circe.generic.semiauto._

final case class DBPool(
  minConnections: DBPoolMinConnections,
  maxConnections: DBPoolMaxConnections,
  leakConnDetectionMillis: LeakConnectionDetectionMillis
)

object DBPool {
  implicit val d: Decoder[DBPool] = deriveDecoder
}

final case class DB(url: DBUrl, user: DBUser, pass: DBPassword, pool: DBPool)

object DB {
  implicit val d: Decoder[DB] = deriveDecoder
}

final case class Server(
  host: ApplicationHost,
  port: ApplicationPort,
  numberParallelHttpRequests: NumberParallelHttpRequests,
  secondsBetweenRequests: SecondsBetweenRequests,
  numberThreadsRabbitPool: NumberThreadsIoPool,
  scappingServiceTimeoutMillis: ScappingServiceTimeoutMillis,
  numberThreadsDbPool: NumberThreadsDbPool
)

object Server {
  implicit val d: Decoder[Server] = deriveDecoder
}

final case class ScrapCoinsJobCronExpression(cronExpression: CronExpr, coinIds: String)

object ScrapCoinsJobCronExpression {
  implicit val d: Decoder[ScrapCoinsJobCronExpression] = deriveDecoder
}

final case class RabbitConfig(host: RabbitHost, port: RabbitPort, user: RabbitUser, pass: RabbitPass)

object RabbitConfig {
  implicit val d: Decoder[RabbitConfig] = deriveDecoder
}

final case class AppConfig(
  server: Server,
  db: DB,
  baseScrappingUri: BaseScrappingUri,
  scrapCoinsJob: ScrapCoinsJobCronExpression,
  rabbit: RabbitConfig
)

object AppConfig {
  implicit val d: Decoder[AppConfig] = deriveDecoder
}
