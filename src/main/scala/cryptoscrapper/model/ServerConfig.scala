package cryptoscrapper.model

import cryptoscrapper.queues.QueueService
import cryptoscrapper.tasks.BackgroundJob
import org.http4s.server.Server

final case class ServerConfig[F[_]](server: Server[F], jobs: List[BackgroundJob[F]], queues: List[QueueService[F]])
