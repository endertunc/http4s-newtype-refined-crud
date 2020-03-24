package car.advert.generators

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import car.advert.model.FuelType
import car.advert.model.OfferType
import car.advert.model.OfferType.New
import car.advert.model.OfferType.Used
import car.advert.model.request.CarAdvert_IN

object CarAdvertInGenerator extends GeneratorsBase {

  val arbFuelType: Arbitrary[String]  = Arbitrary(Gen.oneOf(FuelType.values).map(_.entryName))
  val arbOrderType: Arbitrary[String] = Arbitrary(Gen.oneOf(OfferType.values).map(_.entryName))

  val carAdvertInArbitrary: Arbitrary[CarAdvert_IN] = Arbitrary(Gen.resultOf(CarAdvert_IN.apply _))

  private def newCarAdvertInGen: Gen[CarAdvert_IN] =
    for {
      carAdvert <- carAdvertInArbitrary.arbitrary
      uuid      <- Arbitrary.arbUuid.arbitrary
      fuelType  <- arbFuelType.arbitrary
    } yield carAdvert.copy(id = uuid.toString, offerType = New.entryName, fuelType = fuelType, mileage = None, firstRegistration = None)

  private def usedCarAdvertInGen: Gen[CarAdvert_IN] =
    for {
      carAdvert <- carAdvertInArbitrary.arbitrary.retryUntil(carAdvert => carAdvert.mileage.isDefined && carAdvert.firstRegistration.isDefined)
      uuid      <- Arbitrary.arbUuid.arbitrary
      fuelType  <- arbFuelType.arbitrary
    } yield carAdvert.copy(
      id        = uuid.toString,
      offerType = Used.entryName,
      fuelType  = fuelType
    )

  def generateNewCarAdvertIn: CarAdvert_IN  = newCarAdvertInGen.sample.get
  def generateUsedCarAdvertIn: CarAdvert_IN = usedCarAdvertInGen.sample.get

}
