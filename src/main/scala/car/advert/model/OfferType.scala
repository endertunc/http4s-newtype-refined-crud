package car.advert.model

import scala.collection.immutable

import enumeratum.{ CirceEnum, DoobieEnum, Enum, EnumEntry, QuillEnum }

sealed trait OfferType extends EnumEntry

case object OfferType extends Enum[OfferType] with DoobieEnum[OfferType] with CirceEnum[OfferType] with QuillEnum[OfferType] {

  case object New extends OfferType
  case object Used extends OfferType

  val values: immutable.IndexedSeq[OfferType] = findValues

}
