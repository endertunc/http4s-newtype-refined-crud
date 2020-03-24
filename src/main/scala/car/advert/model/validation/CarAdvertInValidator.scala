package car.advert.model.validation

import cats.data.Validated._
import cats.data.ValidatedNec
import cats.effect.Sync
import cats.implicits._

import car.advert.model._
import car.advert.model.entity.CarAdvert
import car.advert.model.error.AppError.InvalidFieldError
import car.advert.model.request.CarAdvert_IN
import car.advert.model.validation.CarAdvertInValidator.InvalidField
import io.chrisdavenport.log4cats.Logger

trait CarAdvertInValidator {

  type FieldValidationResult[A] = ValidatedNec[InvalidField, A]

  protected def validateId(id: String): ValidatedNec[InvalidField, Id] =
    Id.from(id).fold(_ => InvalidField("id", "must be a valid UUID").invalidNec, _.validNec)

  protected def validateTitle(title: String): FieldValidationResult[Title] =
    Title.from(title).fold(_ => InvalidField("title", "must be a non-empty string").invalidNec, _.validNec)

  // ToDo double check err.getMessage
  protected def validateFuelType(fuelType: String): FieldValidationResult[FuelType] =
    FuelType.withNameInsensitiveEither(fuelType).fold(err => InvalidField("fuelType", err.getMessage).invalidNec, _.validNec)

  protected def validateOfferType(offerType: String): FieldValidationResult[OfferType] =
    OfferType.withNameInsensitiveEither(offerType).fold(err => InvalidField("offerType", err.getMessage).invalidNec, _.validNec)

  protected def validatePrice(price: Int): FieldValidationResult[Price] =
    Price.from(price).fold(_ => InvalidField("price", "must be a non negative integer").invalidNec, _.validNec)

  protected def validateMileage(mileage: Int): FieldValidationResult[Mileage] =
    Mileage.from(mileage).fold(_ => InvalidField("mileage", "must be a non negative integer").invalidNec, _.validNec)

  def validateCarAdvertIn[F[_]: Sync: Logger](
      carAdvert_IN: CarAdvert_IN
  ): F[CarAdvert] = {
    import carAdvert_IN._
    (
      validateId(id),
      validateTitle(title),
      validatePrice(price),
      validateOfferType(offerType),
      validateFuelType(fuelType),
      mileage.traverse(validateMileage),
      firstRegistration.validNec
    ).mapN(CarAdvert.apply)
      .fold(
        InvalidFieldError(_).raiseError[F, CarAdvert],
        _.pure[F]
      )
  }

}

object CarAdvertInValidator {

  case class InvalidField(field: String, message: String) extends ValidationErrors

}
