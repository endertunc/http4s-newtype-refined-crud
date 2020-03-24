package car.advert.repository

import cats.effect.IO

import doobie.scalatest.IOChecker

import eu.timepit.refined.auto._

import org.scalatest.Inspectors.forAll
import org.scalatest.flatspec.AnyFlatSpec

import car.advert.base.TestEmbeddedPostgres
import car.advert.generators.CarAdvertGenerator
import car.advert.http.Pagination
import car.advert.http.Pagination.Limit
import car.advert.http.Pagination.Offset
import car.advert.model.OrderByCriteria
import car.advert.model.OrderByCriteria
import car.advert.model.entity.CarAdvert
import car.advert.repository.postgres.doobie.CarAdvertStatements

class CarAdvertStatementsSpecs extends AnyFlatSpec with IOChecker with TestEmbeddedPostgres {

  override def transactor: doobie.Transactor[IO] = xa

  val carAdvert: CarAdvert   = CarAdvertGenerator.generateNewCarAdvert
  val pagination: Pagination = Pagination(Offset(0L), Limit(20L)) // scalastyle:off magic.number

  "CarAdvertStatements" should "compile" in {
    check(CarAdvertStatements.insert(carAdvert))
    check(CarAdvertStatements.find(carAdvert.id))
    check(CarAdvertStatements.update(carAdvert))
    check(CarAdvertStatements.delete(carAdvert.id))
    forAll(OrderByCriteria.values)(orderByCriteria => check(CarAdvertStatements.list(orderByCriteria, pagination)))
  }

}
