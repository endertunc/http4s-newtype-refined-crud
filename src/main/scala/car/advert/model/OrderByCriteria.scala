package car.advert.model

import scala.collection.immutable

import enumeratum.EnumEntry.LowerCamelcase
import enumeratum._

sealed trait OrderByCriteria extends EnumEntry with LowerCamelcase

case object OrderByCriteria extends Enum[OrderByCriteria] with DoobieEnum[OrderByCriteria] {

  case object Id extends OrderByCriteria
  case object Price extends OrderByCriteria
  case object Mileage extends OrderByCriteria
  case object FirstRegistration extends OrderByCriteria

  val values: immutable.IndexedSeq[OrderByCriteria] = findValues

}
