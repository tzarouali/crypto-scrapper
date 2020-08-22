package cryptoscrapper.repositories

import cryptoscrapper.utils.ApplicationLogger

trait BaseRepository extends ApplicationLogger {
  protected val logger = new Log
}
