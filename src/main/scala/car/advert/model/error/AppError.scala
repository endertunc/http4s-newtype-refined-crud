package car.advert.model.error

import scala.util.control.NoStackTrace

import org.http4s.Status

import cats.data.NonEmptyChain
import cats.implicits._

import car.advert.model.error.Error_OUT.InvalidFieldResponse.InvalidFieldToInvalidFieldResponse
import car.advert.model.error.Error_OUT.SimpleErrorResponse
import car.advert.model.error.Error_OUT.ValidationErrorResponse.ValidationErrorsToValidationErrorResponse
import car.advert.model.validation.CarAdvertInValidator.InvalidField
import car.advert.model.validation.ValidationErrors

sealed trait AppError extends NoStackTrace {
  def message: String
  def status: Status
  def toErrorOut: Error_OUT = SimpleErrorResponse(status.code, message)
}

object AppError {

  final case class InternalServerError(message: String) extends AppError {
    def status: Status = Status.InternalServerError
  }

  final case class NotFound(message: String) extends AppError {
    def status: Status = Status.NotFound
  }

  final case class ValidationError(errors: NonEmptyChain[ValidationErrors]) extends AppError {
    def message: String                = errors.map(_.message).toList.mkString(",")
    def status: Status                 = Status.BadRequest
    override def toErrorOut: Error_OUT = errors.toValidationErrorResponse
  }

  final case class InvalidFieldError(errors: NonEmptyChain[InvalidField]) extends AppError {
    def message: String                = errors.toNonEmptyList.map(e => e.field -> e.message).toList.toMap.toString
    def status: Status                 = Status.BadRequest
    override def toErrorOut: Error_OUT = errors.toInvalidFieldResponse
  }

}
