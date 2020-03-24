package car.advert

import eu.timepit.refined.api.{ Refined, RefinedTypeOps }
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.{ Trimmed, Uuid }
import eu.timepit.refined.types.numeric.PosInt

import io.estatico.newtype.macros.newtype

// Scoverage can't figure out @newtype
// $COVERAGE-OFF$
package object model {

  type RefinedTitle = String Refined (Trimmed And NonEmpty)
  object RefinedTitle extends RefinedTypeOps[RefinedTitle, String]
  @newtype case class Title(value: RefinedTitle)

  object Title {
    def from(title: String): Either[String, Title] = RefinedTitle.from(title).map(Title(_))
    def unsafeFrom(title: String): Title           = Title(RefinedTitle.unsafeFrom(title))
  }

  type RefinedPrice = Int Refined Positive
  object RefinedPrice extends RefinedTypeOps[RefinedPrice, Int]
  @newtype case class Price(value: PosInt)

  object Price {
    def from(price: Int): Either[String, Price] = RefinedPrice.from(price).map(Price(_))
    def unsafeFrom(price: Int): Price           = Price(RefinedPrice.unsafeFrom(price))
  }

  type RefinedMileage = Int Refined Positive
  object RefinedMileage extends RefinedTypeOps[RefinedMileage, Int]
  @newtype case class Mileage(value: RefinedMileage)

  object Mileage {
    def from(mileage: Int): Either[String, Mileage] = RefinedMileage.from(mileage).map(Mileage(_))
    def unsafeFrom(mileage: Int): Mileage           = Mileage(RefinedMileage.unsafeFrom(mileage))
  }

  type RefinedId = String Refined Uuid
  object RefinedId extends RefinedTypeOps[RefinedId, String]
  @newtype case class Id(value: RefinedId)

  object Id {
    def from(id: String): Either[String, Id] = RefinedId.from(id).map(Id(_))
    def unsafeFrom(id: String): Id           = Id(RefinedId.unsafeFrom(id))
  }

}

// $COVERAGE-ON$
