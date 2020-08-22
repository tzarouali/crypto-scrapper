package cryptoscrapper.pools

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

import cats.effect.{Resource, Sync}
import cryptoscrapper.model.AppConfig

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ThreadPools[F[_]: Sync](appConfig: AppConfig) {

  final val rabbitPool: Resource[F, ExecutionContextExecutor] = {
    val SF = implicitly[Sync[F]]
    val alloc = SF.delay(
      Executors.newFixedThreadPool(
        appConfig.server.numberThreadsRabbitPool.value,
        new ThreadFactory {
          private val counter = new AtomicLong(0L)
          def newThread(r: Runnable): Thread = {
            val th = new Thread(r)
            th.setName("rabbit-pool-thread-" + counter.getAndIncrement.toString)
            th.setDaemon(true)
            th
          }
        }
      )
    )
    val free = (es: ExecutorService) => SF.delay(es.shutdown())
    Resource.make(alloc)(free).map(ExecutionContext.fromExecutor)
  }

  final val dbPool: Resource[F, ExecutionContextExecutor] = {
    val SF = implicitly[Sync[F]]
    val alloc = SF.delay(
      Executors.newFixedThreadPool(
        appConfig.server.numberThreadsDbPool.value,
        new ThreadFactory {
          private val counter = new AtomicLong(0L)
          def newThread(r: Runnable): Thread = {
            val th = new Thread(r)
            th.setName("db-pool-thread-" + counter.getAndIncrement.toString)
            th.setDaemon(true)
            th
          }
        }
      )
    )
    val free = (es: ExecutorService) => SF.delay(es.shutdown())
    Resource.make(alloc)(free).map(ExecutionContext.fromExecutor)
  }

}
