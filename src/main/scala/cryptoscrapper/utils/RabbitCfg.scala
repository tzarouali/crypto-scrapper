package cryptoscrapper.utils

import cats.data.NonEmptyList
import cryptoscrapper.model.AppConfig
import dev.profunktor.fs2rabbit.config.{Fs2RabbitConfig, Fs2RabbitNodeConfig}

object RabbitCfg {
  def mk(appConfig: AppConfig): Fs2RabbitConfig = Fs2RabbitConfig(
    virtualHost = "/",
    nodes = NonEmptyList.one(
      Fs2RabbitNodeConfig(
        host = appConfig.rabbit.host.value,
        port = appConfig.rabbit.port.value
      )
    ),
    username = Some(appConfig.rabbit.user.value),
    password = Some(appConfig.rabbit.pass.value),
    ssl = false,
    connectionTimeout = 3,
    requeueOnNack = false,
    internalQueueSize = Some(100),
    automaticRecovery = true
  )
}
