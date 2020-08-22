import java.net.URI

import cats.effect.{ContextShift, IO, Timer}
import cats.scalatest.EitherValues
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import cryptoscrapper.HikariTransactor
import cryptoscrapper.model.AppConfig
import io.circe.config.{parser => configParser}
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.{DSLContext, SQLDialect}
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, OptionValues}

import scala.concurrent.ExecutionContext
import scala.io.Source

abstract class BaseTestSpec
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with EitherValues
    with BeforeAndAfter
    with BeforeAndAfterAll
    with Eventually {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)

  protected val getAppConfig: AppConfig = configParser.decode[AppConfig]().value

  protected def mkDataSource: HikariDataSource = {
    val config = new HikariConfig()
    config.setDriverClassName("org.h2.Driver")
    config.setJdbcUrl("jdbc:h2:mem:cryptoc;MODE=PostgreSQL;DATABASE_TO_UPPER=false;")
    config.setAutoCommit(false)
    config.setUsername(getAppConfig.db.user.value)
    config.setPassword(getAppConfig.db.pass.value)
    config.setMinimumIdle(getAppConfig.db.pool.minConnections.value)
    config.setMaximumPoolSize(getAppConfig.db.pool.maxConnections.value)
    config.setLeakDetectionThreshold(getAppConfig.db.pool.leakConnDetectionMillis.value)
    new HikariDataSource(config)
  }

  protected def mkHikariTransactor(ds: HikariDataSource): HikariTransactor[IO] = new HikariTransactor[IO](ds)

  protected def withInitialData(ds: HikariDataSource, fileUri: URI)(test: DSLContext => Any): Any = {
    val conn                   = ds.getConnection()
    val dslContext: DSLContext = DSL.using(conn, SQLDialect.H2, new Settings())
    val initSqlFile            = Source.fromFile("./sql/init_db.sql")
    val testCaseSqlFile        = Source.fromURI(fileUri)
    try {
      dslContext.execute("drop all objects;create schema if not exists public;set schema public;")
      dslContext.execute(initSqlFile.mkString)
      dslContext.execute(testCaseSqlFile.mkString)
      conn.commit()
      test(dslContext)
    } finally {
      initSqlFile.close()
      testCaseSqlFile.close()
      conn.close()
      ds.close()
    }
  }

}
