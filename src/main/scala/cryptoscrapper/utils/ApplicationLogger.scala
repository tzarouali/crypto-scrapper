package cryptoscrapper.utils

import cryptoscrapper.newtypes.TraceId
import org.slf4j.{Logger, LoggerFactory}

trait ApplicationLogger { self =>

  class Log {
    protected def getLogger: Logger = LoggerFactory.getLogger(self.getClass.getName.stripSuffix("$"))

    def debug(msg: String)(implicit tid: TraceId): Unit = {
      val logger = getLogger
      if (logger.isDebugEnabled) logger.debug(s"[Trace Id - ${tid.value}] $msg") else ()
    }

    def info(msg: String)(implicit tid: TraceId): Unit = {
      val logger = getLogger
      if (logger.isInfoEnabled) logger.info(s"[Trace Id - ${tid.value}] $msg") else ()
    }

    def warn(msg: String)(implicit tid: TraceId): Unit = {
      val logger = getLogger
      if (logger.isWarnEnabled) logger.warn(s"[Trace Id - ${tid.value}] $msg") else ()
    }

    def error(msg: String)(implicit tid: TraceId): Unit = {
      val logger = getLogger
      if (logger.isErrorEnabled) logger.error(s"[Trace Id - ${tid.value}] $msg") else ()
    }

    def error(msg: String, e: Throwable)(implicit tid: TraceId): Unit = {
      val logger = getLogger
      if (logger.isErrorEnabled) logger.error(s"[Trace Id - ${tid.value}] $msg", e) else ()
    }
  }

}
