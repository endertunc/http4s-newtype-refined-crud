package car.advert.model.error

import cats.data.NonEmptyChain

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import car.advert.model.validation.ValidationErrors
import car.advert.model.validation.ValidationErrors

case class CommonError(errors: NonEmptyChain[String])

object CommonError {
  implicit val ErrorCodec: Codec[CommonError] = deriveCodec[CommonError]

  implicit class ValidationErrorsToError(errors: NonEmptyChain[ValidationErrors]) {
    def toError: CommonError = CommonError(errors.map(_.message))
  }
}
