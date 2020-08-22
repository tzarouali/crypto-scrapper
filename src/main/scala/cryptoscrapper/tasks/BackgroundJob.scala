package cryptoscrapper.tasks

import cryptoscrapper.utils.ApplicationLogger

trait BackgroundJob[F[_]] extends ApplicationLogger {

  protected val logger = new Log

  def run: F[Unit]
}
