package car.advert.model.response

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class DeleteCarAdvert_OUT()

object DeleteCarAdvert_OUT {
  implicit val DeleteCarAdvertOutEncoder: Encoder[DeleteCarAdvert_OUT] = deriveEncoder[DeleteCarAdvert_OUT]
}
