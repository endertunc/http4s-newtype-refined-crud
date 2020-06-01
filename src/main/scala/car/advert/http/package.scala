package car.advert

import sttp.tapir.Endpoint

import cats.data.NonEmptyList

package object http {
  type ServerEndpoints = NonEmptyList[Endpoint[_, _, _, Nothing]]
}
