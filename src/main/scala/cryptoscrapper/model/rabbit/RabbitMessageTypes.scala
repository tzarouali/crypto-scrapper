package cryptoscrapper.model.rabbit

object RabbitMessageTypes {
  final val SCRAPPED_COINS_MESSAGE = "SCRAPPED_COINS"
}

object RabbitMessageHeaders {
  final val MESSAGE_TYPE = "MESSAGE_TYPE"
  final val TRACE_ID = "TRACE_ID"
}

object RabbitExchangeAndQueueNames {
  object CoinScrap {
    final val EXCHANGE_NAME = "COINS-EXCHANGE"
    final val ROUTING_KEY = "COINS"
    final val QUEUE_NAME = "COINS"
  }
}