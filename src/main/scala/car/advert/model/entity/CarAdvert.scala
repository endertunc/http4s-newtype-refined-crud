package car.advert.model.entity

import java.time.LocalDate

import io.circe._
import io.circe.generic.semiauto.deriveCodec
import io.circe.refined._

import car.advert.model.FuelType
import car.advert.model.Instances._
import car.advert.model.OfferType
import car.advert.model._

final case class CarAdvert(
    id: Id,
    title: Title,
    price: Price,
    offerType: OfferType,
    fuelType: FuelType,
    mileage: Option[Mileage],
    firstRegistration: Option[LocalDate]
)

object CarAdvert {
  implicit val carAdvertCodec: Codec[CarAdvert] = deriveCodec[CarAdvert]
}
