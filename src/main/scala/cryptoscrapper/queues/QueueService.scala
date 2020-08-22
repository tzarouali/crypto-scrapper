package cryptoscrapper.queues

import cryptoscrapper.utils.ApplicationLogger

trait QueueService[F[_]] extends ApplicationLogger {

  protected val logger = new Log

  def run: F[Unit]

}
