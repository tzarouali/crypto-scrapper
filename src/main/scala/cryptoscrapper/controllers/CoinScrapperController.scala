package cryptoscrapper.controllers

import cats.effect.Sync
import cats.implicits._
import cryptoscrapper.DbTransactor
import cryptoscrapper.model.ApplicationError.{BusinessError, UnexpectedError, ValidationError}
import cryptoscrapper.newtypes._
import cryptoscrapper.services.impl.CoinService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, QueryParamDecoder}

final class CoinScrapperController[F[_]: Sync, E](
  coinService: CoinService[F, E],
  tx: DbTransactor[F, E]
) extends BaseController
    with Http4sDsl[F] {

  implicit val yearQueryParamDecoder: QueryParamDecoder[CoinId] = QueryParamDecoder[Int].map(CoinId.apply)
  object CoinIdQueryParamMatcher extends OptionalMultiQueryParamDecoderMatcher[CoinId]("id")

  private val prefixPath = "/api/coins"

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root :? CoinIdQueryParamMatcher(ids) =>
      implicit val tid: TraceId = extractTraceId(req)
      val idList = ids.toList.flatten
      logger.info(s"Before calling to retrieve details for coins $idList")
      val result = for {
        coinDetails <- tx.transact(coinService.findCoinWithDetails(idList))
        _ = logger.info("After calling to retrieve coin and coin details")
        result <- Ok(coinDetails)
      } yield result

      result.recoverWith {
        case e @ ValidationError(_) => BadRequest(e)
        case e @ BusinessError(_)   => BadRequest(e)
        case e @ UnexpectedError(_) => InternalServerError(e)
      }
  }

  val router: HttpRoutes[F] = Router(
    prefixPath -> routes
  )

}
