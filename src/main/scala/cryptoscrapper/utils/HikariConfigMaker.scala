package cryptoscrapper.utils

import com.zaxxer.hikari.HikariConfig
import cryptoscrapper.model.AppConfig

object HikariConfigMaker {
  def makeHikariConfig(appConfig: AppConfig): HikariConfig = {
    val config = new HikariConfig()
    config.setJdbcUrl(appConfig.db.url.value)
    config.setAutoCommit(false)
    config.setUsername(appConfig.db.user.value)
    config.setPassword(appConfig.db.pass.value)
    config.setMinimumIdle(appConfig.db.pool.minConnections.value)
    config.setMaximumPoolSize(appConfig.db.pool.maxConnections.value)
    config.setLeakDetectionThreshold(appConfig.db.pool.leakConnDetectionMillis.value)
    config
  }
}
