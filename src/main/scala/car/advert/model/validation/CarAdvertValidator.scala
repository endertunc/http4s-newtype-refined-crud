package car.advert.model.validation

import java.time.LocalDate

import cats.data.{ NonEmptyChain, Validated, ValidatedNec }
import cats.effect.Sync
import cats.implicits._

import car.advert.model.OfferType.New
import car.advert.model.OfferType.Used
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.model.error.AppError.ValidationError
import car.advert.model.validation.CarAdvertValidator.InvalidDomainState
import io.chrisdavenport.log4cats.Logger

trait CarAdvertValidator {

  type ValidationResult[A] = ValidatedNec[InvalidDomainState, A]

  def validateMileage(carAdvert: CarAdvert): ValidationResult[Unit] = {
    def validateNewCarAdvertAndMileage: Validated[NonEmptyChain[InvalidDomainState], Unit] =
      if (carAdvert.offerType == New && carAdvert.mileage.isDefined) {
        InvalidDomainState("New advert can not have mileage").invalidNec
      } else {
        ().validNec
      }

    def validateUsedCarAdvertAndMileage: Validated[NonEmptyChain[InvalidDomainState], Unit] =
      if (carAdvert.offerType == Used && carAdvert.mileage.isEmpty) {
        InvalidDomainState("Used car advert must have mileage").invalidNec
      } else {
        ().validNec
      }

    (
      validateNewCarAdvertAndMileage,
      validateUsedCarAdvertAndMileage
    ).tupled.map(_ => ())

  }

  protected def validateFirstRegistration(carAdvert: CarAdvert): ValidationResult[Unit] = {
    def validateNewCarAdvertAndFirstRegistration: Validated[NonEmptyChain[InvalidDomainState], Unit] =
      if (carAdvert.offerType == car.advert.model.OfferType.New && carAdvert.firstRegistration.isDefined) {
        InvalidDomainState("New car advert can not have first registration date").invalidNec
      } else {
        ().validNec
      }

    def validateUsedCarAdvertAndFirstRegistration: Validated[NonEmptyChain[InvalidDomainState], Unit] =
      if (carAdvert.offerType == car.advert.model.OfferType.Used && carAdvert.firstRegistration.isEmpty) {
        InvalidDomainState("Used car advert must have first registration date").invalidNec
      } else {
        ().validNec
      }

    def validateFirstRegistrationIsNotInFuture(firstRegistration: LocalDate): ValidationResult[Unit] =
      if (firstRegistration.isAfter(LocalDate.now())) {
        InvalidDomainState("First registration date can not be in future").invalidNec
      } else {
        ().validNec
      }

    (
      validateNewCarAdvertAndFirstRegistration,
      validateUsedCarAdvertAndFirstRegistration,
      carAdvert.firstRegistration.traverse(validateFirstRegistrationIsNotInFuture)
    ).tupled.map(_ => ())

  }

  def validateCarAdvert[F[_]: Sync: Logger](carAdvert: CarAdvert): F[Unit] =
    (validateMileage(carAdvert), validateFirstRegistration(carAdvert)).tupled
      .fold(
        { nec =>
          val err = ValidationError(nec)
          Logger[F].info(err.message) *> ValidationError(nec).raiseError[F, Unit]
        },
        _ => ().pure[F]
      )

}

object CarAdvertValidator {

  // For some reason I cant do this WTF
  //  sealed trait DomainError extends ValidationErrors

  final case class InvalidDomainState(message: String) extends ValidationErrors
}
