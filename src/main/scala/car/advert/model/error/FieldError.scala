package car.advert.model.error

import cats.data.NonEmptyChain

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import car.advert.model.validation.CarAdvertInValidator.InvalidField
import car.advert.model.validation.CarAdvertInValidator.InvalidField

case class FieldError(errors: Map[String, String])

object FieldError {
  implicit val ErrorCodec: Codec[FieldError] = deriveCodec[FieldError]

  implicit class FieldValidationErrorsToFieldError(errors: NonEmptyChain[InvalidField]) {
    def toFieldError: FieldError = FieldError(errors.toNonEmptyList.toList.map(e => e.field -> e.message).toMap)
  }
}
