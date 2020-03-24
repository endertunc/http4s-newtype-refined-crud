package car.advert.http

import org.http4s._
import org.http4s.circe.{ CirceEntityDecoder, CirceEntityEncoder }
import org.http4s.implicits._

import cats.NonEmptyTraverse.ops.toAllNonEmptyTraverseOps
import cats.effect.IO
import cats.implicits._

import eu.timepit.refined.auto._

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import car.advert.base.ServicesAndRepos
import car.advert.generators.CarAdvertGenerator
import car.advert.generators.CarAdvertInGenerator
import car.advert.generators.CarAdvertInGenerator
import car.advert.model.OrderByCriteria
import car.advert.model.OrderByCriteria
import car.advert.model.OrderByCriteria
import car.advert.model.OrderByCriteria.FirstRegistration
import car.advert.model.OrderByCriteria.FirstRegistration
import car.advert.model.OrderByCriteria.Mileage
import car.advert.model.OrderByCriteria.Mileage
import car.advert.model.OrderByCriteria.Price
import car.advert.model.OrderByCriteria.Price
import car.advert.model.OrderByCriteria.Price
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.model.error.CommonError
import car.advert.quill.CarAdverts
import car.advert.quill.CarAdverts

class CarAdvertRoutesSpec extends AsyncFlatSpec with ServicesAndRepos with Matchers with CirceEntityDecoder with CirceEntityEncoder {

  implicit def criteriaQueryParamDecoder: QueryParamEncoder[OrderByCriteria] = (value: OrderByCriteria) => QueryParameterValue(value.entryName)

  private val root: Uri = uri""

  "Car advert" should "allow to create car advert" in ioSuit {
    for {
      carAdvert <- CarAdvertInGenerator.generateNewCarAdvertIn.pure[IO]
      request   <- Request[IO](method = Method.POST, uri = root, body = carAdvert)
      response  <- ctx.httpApp.run(request)
      _         <- response.as[CarAdvert]
    } yield response.status shouldBe Status.Ok
  }

  it should "get car advert by id" in ioSuit {
    for {
      carAdvert          <- CarAdverts.insert(CarAdvertGenerator.generateNewCarAdvert)
      request            <- Request[IO](method = Method.GET, uri = root / carAdvert.id.value.value)
      response           <- ctx.httpApp.run(request)
      retrievedCarAdvert <- response.as[CarAdvert]
    } yield {
      response.status shouldBe Status.Ok
      retrievedCarAdvert shouldBe carAdvert
    }
  }

  it should "return bad request when car advert does not exist" in ioSuit {
    for {
      nonExistCarAdvert <- CarAdvertGenerator.generateNewCarAdvert.pure[IO]
      request           <- Request[IO](method = Method.GET, uri = root / nonExistCarAdvert.id.value.value)
      response          <- ctx.httpApp.run(request)
      commonError       <- response.as[CommonError]
    } yield {
      response.status shouldBe Status.NotFound
      commonError.errors.length shouldBe 1
    }
  }

  it should "delete car advert by id" in ioSuit {
    for {
      carAdvert      <- CarAdverts.insert(CarAdvertGenerator.generateNewCarAdvert)
      request        <- Request[IO](method = Method.DELETE, uri = root / carAdvert.id.value.value)
      response       <- ctx.httpApp.run(request)
      _              <- IO(response.status shouldBe Status.Ok)
      maybeCarAdvert <- CarAdverts.findById(carAdvert.id)
    } yield maybeCarAdvert should not be defined
  }

  it should "update car advert by id" in ioSuit {
    for {
      existingCarAdvert  <- CarAdverts.insert(CarAdvertGenerator.generateNewCarAdvert)
      updatedCarAdvert   <- IO(CarAdvertGenerator.generateNewCarAdvert.copy(id = existingCarAdvert.id))
      request            <- Request[IO](method = Method.PUT, uri = root, body = updatedCarAdvert)
      response           <- ctx.httpApp.run(request)
      retrievedCarAdvert <- response.as[CarAdvert]
    } yield {
      response.status shouldBe Status.Ok
      retrievedCarAdvert shouldBe updatedCarAdvert
    }
  }

  it should "order list car adverts by price" in ioSuit {

    for {
      insertedAdverts <- List(
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert
      ).traverse(CarAdverts.insert)
      criteria   <- (Price: OrderByCriteria).pure[IO]
      request    <- Request[IO](method = Method.GET, uri = root +? ("orderBy", criteria))
      response   <- ctx.httpApp.run(request)
      carAdverts <- response.as[List[CarAdvert]]
    } yield {
      response.status shouldBe Status.Ok
      val expectedResult = insertedAdverts.sortWith(_.price.value.value < _.price.value.value)
      carAdverts should contain theSameElementsInOrderAs expectedResult
    }
  }

  it should "order list car adverts by firstRegistration" in ioSuit {
    for {
      insertedAdverts <- List(
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateNewCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateNewCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert
      ).traverse(CarAdverts.insert)
      criteria   <- (FirstRegistration: OrderByCriteria).pure[IO]
      request    <- Request[IO](method = Method.GET, uri = root +? ("orderBy", criteria))
      response   <- ctx.httpApp.run(request)
      carAdverts <- response.as[List[CarAdvert]]
    } yield {
      response.status shouldBe Status.Ok
      val sortedResult: List[CarAdvert]                                 = insertedAdverts.sortBy(_.firstRegistration.map(_.toEpochDay))
      val (sortedWithFirstRegistration, sortedWithoutFirstRegistration) = sortedResult.partition(_.firstRegistration.isDefined)
      val expectedResult: List[CarAdvert]                               = sortedWithFirstRegistration ++ sortedWithoutFirstRegistration
      carAdverts should contain theSameElementsInOrderAs expectedResult
    }
  }

  it should "order list car adverts by mileage" in ioSuit {
    for {
      insertedAdverts <- List(
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert.copy(mileage = None),
        CarAdvertGenerator.generateUsedCarAdvert,
        CarAdvertGenerator.generateUsedCarAdvert.copy(mileage = None),
        CarAdvertGenerator.generateUsedCarAdvert
      ).traverse(CarAdverts.insert)
      criteria   <- (Mileage: OrderByCriteria).pure[IO]
      request    <- Request[IO](method = Method.GET, uri = root +? ("orderBy", criteria))
      response   <- ctx.httpApp.run(request)
      carAdverts <- response.as[List[CarAdvert]]
    } yield {
      response.status shouldBe Status.Ok

      val sortedResult: List[CarAdvert]             = insertedAdverts.sortBy(_.mileage.map(_.value.value))
      val (sortedWithMileage, sortedWithoutMileage) = sortedResult.partition(_.mileage.isDefined)
      val expectedResult: List[CarAdvert]           = sortedWithMileage ++ sortedWithoutMileage

      carAdverts should contain theSameElementsInOrderAs expectedResult
    }
  }

  //  it should "order list car adverts by fuelType" in ioSuit {
  //
  //    for {
  //      insertedAdverts <- List(
  //        CarAdvertGenerator.generateUsedCarAdvert.copy(fuelType = Gasoline),
  //        CarAdvertGenerator.generateUsedCarAdvert.copy(fuelType = Diesel),
  //        CarAdvertGenerator.generateUsedCarAdvert.copy(fuelType = Gasoline),
  //        CarAdvertGenerator.generateUsedCarAdvert.copy(fuelType = Diesel),
  //        CarAdvertGenerator.generateUsedCarAdvert.copy(fuelType = Gasoline)
  //      ).traverse(CarAdverts.insert)
  //      criteria   <- (FuelType: OrderByCriteria).pure[IO]
  //      request    <- Request[IO](method = Method.GET, uri = root +? ("orderBy", criteria))
  //      response   <- ctx.httpApp.run(request)
  //      carAdverts <- response.as[List[CarAdvert]]
  //    } yield {
  //      response.status shouldBe Status.Ok
  //      val expectedResult = insertedAdverts.sortWith(_.fuelType.entryName < _.fuelType.entryName)
  //      carAdverts should contain theSameElementsInOrderAs expectedResult
  //    }
  //  }
  //
  //  it should "order list car adverts by offerType" in ioSuit {
  //
  //    for {
  //      insertedAdverts <- List(
  //        CarAdvertGenerator.generateUsedCarAdvert,
  //        CarAdvertGenerator.generateNewCarAdvert,
  //        CarAdvertGenerator.generateUsedCarAdvert,
  //        CarAdvertGenerator.generateNewCarAdvert,
  //        CarAdvertGenerator.generateUsedCarAdvert
  //      ).traverse(CarAdverts.insert)
  //      criteria   <- (OfferType: OrderByCriteria).pure[IO]
  //      request    <- Request[IO](method = Method.GET, uri = root +? ("orderBy", criteria))
  //      response   <- ctx.httpApp.run(request)
  //      carAdverts <- response.as[List[CarAdvert]]
  //    } yield {
  //      response.status shouldBe Status.Ok
  //      val expectedResult = insertedAdverts.sortWith(_.offerType.entryName < _.offerType.entryName)
  //      carAdverts should contain theSameElementsInOrderAs expectedResult
  //    }
  //  }

}

//  import scala.math.Ordered._
//  import io.circe.Encoder
//  import io.circe.syntax._
//
//  import scala.math.Ordered._
//
//  val d: List[CarAdvert] = insertedAdverts.sortWith { (a, b) =>
//    (a.mileage.map(_.value.value), b.mileage.map(_.value.value)) match {
//      case (_, None)          => false
//      case (None, _)          => true
//      case (Some(x), Some(y)) => x < y
//    }
//  }
//  expectedResult.foreach(x => println(x.asJson.spaces2))
//  println("-----------")
//  carAdverts.foreach(x => println(x.asJson.spaces2))
