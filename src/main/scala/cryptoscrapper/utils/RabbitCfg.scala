package cryptoscrapper.utils

import cats.data.NonEmptyList
import com.rabbitmq.client.ConnectionFactory
import cryptoscrapper.model.AppConfig
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}

object RabbitCfg {
  def mk(appConfig: AppConfig): Fs2RabbitConfig = Fs2RabbitConfig(
    nodes = NonEmptyList.one(
      Fs2RabbitNodeConfig(
        host = appConfig.rabbit.host.value,
        port = appConfig.rabbit.port.value
      )
    ),
    virtualHost = "/",
    connectionTimeout = 3,
    ssl = false,
    username = Some(appConfig.rabbit.user.value),
    password = Some(appConfig.rabbit.pass.value),
    requeueOnNack = false,
    requeueOnReject = false,
    internalQueueSize = Some(100),
    requestedHeartbeat = ConnectionFactory.DEFAULT_HEARTBEAT,
    automaticRecovery = true,
  )
}
