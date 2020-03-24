package car.advert.model.request

import java.time.LocalDate

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CarAdvert_IN(
    id: String,
    title: String,
    price: Int,
    offerType: String,
    fuelType: String,
    mileage: Option[Int],
    firstRegistration: Option[LocalDate]
)

object CarAdvert_IN {
  implicit val carAdvertInCodec: Codec[CarAdvert_IN] = deriveCodec[CarAdvert_IN]

}
