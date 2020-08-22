package cryptoscrapper

import java.sql.Connection

import cats.data.Kleisli
import cats.effect.{Resource, Sync}
import cats.implicits._
import com.zaxxer.hikari.HikariDataSource
import cryptoscrapper.newtypes.TraceId
import cryptoscrapper.utils.{ApplicationLogger, JooqQueryListener}
import org.jooq.impl.DSL
import org.jooq.{DSLContext, SQLDialect}

class HikariTransactor[F[_]: Sync](ds: HikariDataSource) extends DbTransactor[F, DSLContext] with ApplicationLogger {

  private val logger = new Log

  override def transact[A](b: Kleisli[F, DSLContext, A])(implicit tid: TraceId): F[A] =
    Resource.fromAutoCloseable[F, Connection](Sync[F].delay(ds.getConnection)).use { c =>
      val ctx = DSL.using(c, SQLDialect.POSTGRES)
      val cfg = ctx.configuration()
      cfg.set(new JooqQueryListener(tid))
      b.run(ctx).attempt.flatMap {
        case Left(e) =>
          logger.error("Caught error. Proceeding to rollback", e)
          for {
            _ <- Sync[F].delay(c.rollback())
            _ = logger.error("Rollbacked transaction from an unexpected error executing query")
            r <- Sync[F].raiseError[A](e)
          } yield r
        case Right(v) =>
          for {
            _ <- Sync[F].delay(c.commit())
            r <- v.pure[F]
          } yield r
      }
    }

}
