package car.advert.generators

import eu.timepit.refined.api.RefType
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.scalacheck.all._
import eu.timepit.refined.scalacheck.arbitraryRefType
import eu.timepit.refined.string.Trimmed
import eu.timepit.refined.string.Uuid

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import car.advert.model.OfferType.{ New, Used }
import car.advert.model.entity.CarAdvert
import enumeratum.scalacheck._

object CarAdvertGenerator extends GeneratorsBase {

  implicit def nonEmptyTrimmedStringArbitrary[F[_, _]](implicit rt: RefType[F]): Arbitrary[F[String, (Trimmed And NonEmpty)]] =
    arbitraryRefType(arbAlphaString.arbitrary.retryUntil(_.nonEmpty))

  implicit def uuidArbitrary[F[_, _]](
      implicit rt: RefType[F]
  ): Arbitrary[F[String, Uuid]] =
    arbitraryRefType(Arbitrary.arbUuid.arbitrary.map(_.toString))

  val carAdvertInArbitrary: Arbitrary[CarAdvert] = Arbitrary(Gen.resultOf(CarAdvert.apply _))

  private def newCarAdvertGen: Gen[CarAdvert] =
    for {
      carAdvert <- carAdvertInArbitrary.arbitrary
    } yield carAdvert.copy(offerType = New, mileage = None, firstRegistration = None)

  private def usedCarAdvertGen: Gen[CarAdvert] =
    for {
      carAdvert <- carAdvertInArbitrary.arbitrary.retryUntil(carAdvert => carAdvert.mileage.isDefined && carAdvert.firstRegistration.isDefined)
    } yield carAdvert.copy(offerType = Used)

  def generateNewCarAdvert: CarAdvert  = newCarAdvertGen.sample.get
  def generateUsedCarAdvert: CarAdvert = usedCarAdvertGen.sample.get

}
