package car.advert.model.error

import cats.data.NonEmptyChain

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import car.advert.model.validation.CarAdvertInValidator.InvalidField
import car.advert.model.validation.ValidationErrors

sealed trait Error_OUT

object Error_OUT {

  case class InvalidFieldResponse(errors: Map[String, String]) extends Error_OUT
  object InvalidFieldResponse {
    implicit val ErrorCodec: Codec[InvalidFieldResponse] = deriveCodec[InvalidFieldResponse]

    implicit class InvalidFieldToInvalidFieldResponse(errors: NonEmptyChain[InvalidField]) {
      def toInvalidFieldResponse: InvalidFieldResponse = InvalidFieldResponse(errors.toNonEmptyList.toList.map(e => e.field -> e.message).toMap)
    }
  }

  case class SimpleErrorResponse(status: Int, message: String) extends Error_OUT
  object SimpleErrorResponse {
    implicit val ErrorCodec: Codec[SimpleErrorResponse] = deriveCodec[SimpleErrorResponse]
  }

  case class ValidationErrorResponse(errors: List[String]) extends Error_OUT
  object ValidationErrorResponse {
    implicit val ErrorCodec: Codec[ValidationErrorResponse] = deriveCodec[ValidationErrorResponse]

    implicit class ValidationErrorsToValidationErrorResponse(errors: NonEmptyChain[ValidationErrors]) {
      def toValidationErrorResponse: ValidationErrorResponse = ValidationErrorResponse(errors.toNonEmptyList.toList.map(_.message))
    }

  }

}
