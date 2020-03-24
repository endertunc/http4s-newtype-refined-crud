package car.advert.model

import scala.collection.immutable

import enumeratum._

sealed trait FuelType extends EnumEntry

case object FuelType extends Enum[FuelType] with DoobieEnum[FuelType] with CirceEnum[FuelType] with QuillEnum[FuelType] {

  case object Gasoline extends FuelType
  case object Diesel extends FuelType

  val values: immutable.IndexedSeq[FuelType] = findValues

}
