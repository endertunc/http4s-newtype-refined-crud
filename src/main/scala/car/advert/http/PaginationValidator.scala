package car.advert.http

import cats.data.ValidatedNec
import cats.effect.Sync
import cats.implicits._

import eu.timepit.refined.cats._

import car.advert.http.Pagination.Limit
import car.advert.http.Pagination.MaxLimit
import car.advert.http.Pagination.Offset
import car.advert.http.Pagination.RefinedLimit
import car.advert.http.Pagination.RefinedOffset
import car.advert.http.PaginationValidator.{ InvalidLimit, InvalidOffset, PaginationError }
import car.advert.model.Instances._
import car.advert.model.error.AppError.ValidationError
import car.advert.model.validation.ValidationErrors
import car.advert.model.validation.ValidationErrors
import io.chrisdavenport.log4cats.Logger

trait PaginationValidator {

  type PaginationValidationResult[A] = ValidatedNec[PaginationError, A]

  def validate(page: Long, pageSize: Long): PaginationValidationResult[Pagination] =
    (validateOffset(page), validateLimit(pageSize)).mapN(Pagination.apply)

  def validatePagination[F[_]: Sync: Logger](
      offset: Long,
      limit: Long
  ): F[Pagination] =
    (validateOffset(offset), validateLimit(limit)).tupled
      .fold(
        { nec =>
          val err = ValidationError(nec)
          Logger[F].info(err.message) *> err.raiseError[F, Pagination]
        }, {
          case (offset, limit) => car.advert.http.Pagination(offset, limit).pure[F]
        }
      )

  private def validateOffset(offset: Long): PaginationValidationResult[Offset] =
    RefinedOffset.from(offset).bifoldMap(_ => InvalidOffset.invalidNec[Offset], refinedOffset => Offset(refinedOffset).validNec[PaginationError])

  private def validateLimit(limit: Long): PaginationValidationResult[Limit] =
    RefinedLimit.from(limit).bifoldMap(_ => InvalidLimit.invalidNec[Limit], refinedLimit => Limit(refinedLimit).validNec[PaginationError])

}

object PaginationValidator {
  sealed trait PaginationError extends ValidationErrors

  case object InvalidOffset extends PaginationError {
    override val message: String = "Offset must be greater than or equal to 0"
  }

  case object InvalidLimit extends PaginationError {
    override val message: String = s"Limit size must be from 0 to $MaxLimit"
  }
}
