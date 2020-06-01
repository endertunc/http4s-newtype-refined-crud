package car.advert.model.validation

import java.util.UUID

import scala.util.control.NonFatal

import org.http4s.circe.CirceEntityDecoder

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.implicits._
import cats.scalatest.ValidatedMatchers
import cats.scalatest.ValidatedValues

import io.circe._
import io.circe.literal._
import io.circe.syntax._

import eu.timepit.refined.auto._

import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import Inspectors._
import car.advert.generators.GeneratorsBase
import car.advert.model.FuelType
import car.advert.model.OfferType
import car.advert.model.error.AppError.InvalidFieldError
import car.advert.model.error.Error_OUT
import car.advert.model.error.Error_OUT.InvalidFieldResponse
import car.advert.model.request.CarAdvert_IN
import car.advert.model.validation.CarAdvertInValidator.InvalidField
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class CarAdvertInValidationSpec
    extends AsyncFlatSpec
    with CarAdvertValidator
    with CarAdvertInValidator
    with Matchers
    with ValidatedMatchers
    with ValidatedValues
    with GeneratorsBase
    with CirceEntityDecoder {

  // Move to base and inherit it
  implicit lazy val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.create[IO].unsafeRunSync

  val invalidUUId         = "invalidUUId"
  val invalidTitle        = ""
  val invalidPrice: Int   = arbNegativeInt.sample.get
  val invalidMileage: Int = arbNegativeInt.sample.get
  val invalidOfferType    = "invalidOfferType"
  val invalidFuelType     = "invalidFuelType"

  "CarAdvertInValidator" should "validate Id" in {
    validateId(invalidUUId) should beInvalid(NonEmptyChain.one(InvalidField("id", "must be a valid UUID")))
    validateId(UUID.randomUUID().toString) should be(valid)
  }

  it should "validate Title" in {
    validateTitle(invalidTitle) should beInvalid(NonEmptyChain.one(InvalidField("title", "must be a non-empty string")))
    validateTitle(arbAlphaString.arbitrary.sample.get) should be(valid)
  }

  it should "validate Price" in {
    validatePrice(invalidPrice) should beInvalid(NonEmptyChain.one(InvalidField("price", "must be a non negative integer")))
    validatePrice(arbPositiveInt.arbitrary.sample.get) should be(valid)
  }

  it should "validate Mileage" in {
    validateMileage(invalidMileage) should beInvalid(NonEmptyChain.one(InvalidField("mileage", "must be a non negative integer")))
    validateMileage(arbPositiveInt.arbitrary.sample.get) should be(valid)
  }

  it should "validate OfferType" in {
    validateOfferType(invalidOfferType) should be(invalid)
    forAll(OfferType.values)(offerType => validateOfferType(offerType.entryName) should be(valid))
  }

  it should "validate FuelType" in {
    validateFuelType(invalidFuelType) should be(invalid)
    forAll(FuelType.values)(fuelType => validateFuelType(fuelType.entryName) should be(valid))
  }

  it should "report errors" in {
    val carAdvertIn = CarAdvert_IN(invalidUUId, invalidTitle, invalidPrice, invalidOfferType, invalidFuelType, Some(invalidMileage), None)

    val expectedJson: Json =
      json"""
        {
          "errors": {
            "mileage": "must be a non negative integer",
            "fuelType": "invalidFuelType is not a member of Enum (Gasoline, Diesel)",
            "price": "must be a non negative integer",
            "offerType": "invalidOfferType is not a member of Enum (New, Used)",
            "id": "must be a valid UUID",
            "title": "must be a non-empty string"
          }
        }
        """

    val validationResult: IO[Assertion] =
      validateCarAdvertIn[IO](carAdvertIn).flatMap(_ => IO[Assertion](fail("invalid car advert passed validation"))).recoverWith {
        case invalidFieldError: InvalidFieldError =>
          invalidFieldError.toErrorOut match {
            case invalidFieldResponse: InvalidFieldResponse =>
              IO[Assertion](invalidFieldResponse.asJson shouldBe expectedJson)
            case e: Error_OUT => IO[Assertion](fail(s"CarAdvert_IN validation failed with unexpected Error_OUT $e"))
          }
        case NonFatal(e) => IO[Assertion](fail("CarAdvert_IN validation failed with unexpected exception.", e))
      }
    validationResult.unsafeToFuture()
  }

}
