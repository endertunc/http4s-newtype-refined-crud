package car.advert.http

import org.http4s.QueryParamDecoder._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

import cats.effect.Sync

import eu.timepit.refined.api.RefinedTypeOps
import eu.timepit.refined.types.numeric.NonNegLong

import Pagination.Limit
import Pagination.Offset
import io.chrisdavenport.log4cats.Logger
import io.estatico.newtype.macros.newtype

case class Pagination(offset: Offset, limit: Limit)

object Pagination extends PaginationValidator {

  def fromParams[F[_]: Sync: Logger](maybeOffset: Option[Long], maybeLimit: Option[Long]): F[Pagination] = {
    val offset = maybeOffset.getOrElse(Pagination.DefaultOffset)
    val limit  = maybeLimit.getOrElse(Pagination.DefaultLimit)
    validatePagination(offset, limit)
  }

  def default[F[_]: Sync: Logger](): F[Pagination] = fromParams(None, None)

  object RefinedOffset extends RefinedTypeOps[NonNegLong, Long]
  @newtype case class Offset(value: NonNegLong)

  object RefinedLimit extends RefinedTypeOps[NonNegLong, Long]
  @newtype case class Limit(value: NonNegLong)

  object OffsetMatcher extends OptionalQueryParamDecoderMatcher[Long]("offset")
  object LimitMatcher extends OptionalQueryParamDecoderMatcher[Long]("limit")

  val DefaultOffset: Long = 0
  val DefaultLimit: Long  = 20
  val MaxLimit: Long      = 100

}
