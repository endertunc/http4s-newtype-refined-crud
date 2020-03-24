package car.advert.model.validation

import java.time.LocalDate

import scala.util.control.NonFatal

import org.http4s.circe.CirceEntityDecoder

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.implicits._
import cats.scalatest.ValidatedMatchers
import cats.scalatest.ValidatedValues

import io.circe.Json
import io.circe.literal._

import eu.timepit.refined.auto._

import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import car.advert.generators.CarAdvertGenerator
import car.advert.generators.GeneratorsBase
import car.advert.model.Mileage
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.model.error.AppError.ValidationError
import car.advert.model.validation.CarAdvertValidator.InvalidDomainState
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class CarAdvertValidationSpec
    extends AsyncFlatSpec
    with CarAdvertValidator
    with Matchers
    with ValidatedMatchers
    with ValidatedValues
    with GeneratorsBase
    with CirceEntityDecoder {

  // Move to base and inherit it
  implicit lazy val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.create[IO].unsafeRunSync

  "CarAdvertValidator" should "validate mileage" in {
    val newCarAdvertWithMileage: CarAdvert = CarAdvertGenerator.generateNewCarAdvert.copy(mileage = Some(Mileage(1)))
    validateMileage(newCarAdvertWithMileage) should beInvalid(NonEmptyChain.one(InvalidDomainState("New advert can not have mileage")))

    val usedCarAdvertWithoutMileage: CarAdvert = CarAdvertGenerator.generateUsedCarAdvert.copy(mileage = None)
    validateMileage(usedCarAdvertWithoutMileage) should beInvalid(NonEmptyChain.one(InvalidDomainState("Used car advert must have mileage")))

  }

  it should "validate firstRegistration" in {
    val newCarAdvertWithFirstRegistration: CarAdvert = CarAdvertGenerator.generateNewCarAdvert.copy(firstRegistration = Some(LocalDate.now()))
    validateFirstRegistration(newCarAdvertWithFirstRegistration) should beInvalid(
      NonEmptyChain.one(InvalidDomainState("New car advert can not have first registration date"))
    )

    val usedCarAdvertWithoutFirstRegistration: CarAdvert = CarAdvertGenerator.generateUsedCarAdvert.copy(firstRegistration = None)
    validateFirstRegistration(usedCarAdvertWithoutFirstRegistration) should beInvalid(
      NonEmptyChain.one(InvalidDomainState("Used car advert must have first registration date"))
    )

    val usedCarAdvertWithFutureFirstRegistration: CarAdvert =
      CarAdvertGenerator.generateUsedCarAdvert.copy(firstRegistration = Some(LocalDate.now().plusDays(1)))
    validateFirstRegistration(usedCarAdvertWithFutureFirstRegistration) should beInvalid(
      NonEmptyChain.one(InvalidDomainState("First registration date can not be in future"))
    )
  }

  it should "report errors in new car advert" in {
    val newCarAdvertWithMileageAndFirstRegistration =
      CarAdvertGenerator.generateNewCarAdvert.copy(firstRegistration = Some(LocalDate.now()), mileage = Some(Mileage(1)))

    val expectedJson: Json =
      json"""
          {
            "errors" : [
              "New advert can not have mileage",
              "New car advert can not have first registration date"
            ]
          }
        """
    val validationResult: IO[Assertion] =
      validateCarAdvert[IO](newCarAdvertWithMileageAndFirstRegistration)
        .flatMap(_ => IO[Assertion](fail("invalid car advert passed validation")))
        .recoverWith {
          case e: ValidationError =>
            for {
              response     <- e.toHttpResponse[IO]
              jsonResponse <- response.as[Json]
            } yield {
              jsonResponse shouldBe expectedJson
            }
          case NonFatal(e) => IO[Assertion](fail("CarAdvert_IN validation failed with unexpected exception.", e))
        }
    validationResult.unsafeToFuture()
  }

  it should "report errors in used car advert" in {
    val usedCarAdvertWithoutMileageAndFirstRegistration = CarAdvertGenerator.generateUsedCarAdvert.copy(firstRegistration = None, mileage = None)

    val expectedJson: Json =
      json"""
          {
            "errors" : [
              "Used car advert must have mileage",
              "Used car advert must have first registration date"
            ]
          }
        """
    val validationResult: IO[Assertion] =
      validateCarAdvert[IO](usedCarAdvertWithoutMileageAndFirstRegistration)
        .flatMap(_ => IO[Assertion](fail("invalid car advert passed validation")))
        .recoverWith {
          case e: ValidationError =>
            for {
              response     <- e.toHttpResponse[IO]
              jsonResponse <- response.as[Json]
            } yield {
              jsonResponse shouldBe expectedJson
            }
          case NonFatal(e) => IO[Assertion](fail("CarAdvert_IN validation failed with unexpected exception.", e))
        }
    validationResult.unsafeToFuture()
  }

}
