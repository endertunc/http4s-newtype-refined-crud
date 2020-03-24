package car.advert.model.error

import scala.util.control.NoStackTrace

import org.http4s.circe.jsonEncoderOf
import org.http4s.{ Response, Status }

import cats.Applicative
import cats.Applicative
import cats.data.NonEmptyChain
import cats.data.NonEmptyChain
import cats.implicits._

import car.advert.model.error.CommonError.ValidationErrorsToError
import car.advert.model.error.FieldError.FieldValidationErrorsToFieldError
import car.advert.model.validation.CarAdvertInValidator.InvalidField
import car.advert.model.validation.CarAdvertInValidator.InvalidField
import car.advert.model.validation.ValidationErrors
import car.advert.model.validation.ValidationErrors

sealed trait AppError extends NoStackTrace {
  def message: String
  def status: Status
  def toHttpResponse[F[_]]()(implicit F: Applicative[F]): F[Response[F]] =
    Response(status)
      .withEntity(CommonError(NonEmptyChain.one(message)))(jsonEncoderOf[F, CommonError])
      .pure[F]
}

object AppError {

  final case class NotFound(message: String) extends AppError {
    def status: Status = Status.NotFound
  }

  final case class ValidationError(errors: NonEmptyChain[ValidationErrors]) extends AppError {
    def message: String = errors.map(_.message).toList.mkString(",")
    def status: Status  = Status.BadRequest
    override def toHttpResponse[F[_]]()(implicit F: Applicative[F]): F[Response[F]] =
      Response(status)
        .withEntity(errors.toError)(jsonEncoderOf[F, CommonError])
        .pure[F]
  }

  // ToDo update this
  final case class InvalidFieldError(errors: NonEmptyChain[InvalidField]) extends AppError {
    def message: String = errors.toNonEmptyList.map(e => e.field -> e.message).toList.toMap.toString
    def status: Status  = Status.BadRequest
    override def toHttpResponse[F[_]]()(implicit F: Applicative[F]): F[Response[F]] =
      Response(status)
        .withEntity(errors.toFieldError)(jsonEncoderOf[F, FieldError])
        .pure[F]
  }

}
