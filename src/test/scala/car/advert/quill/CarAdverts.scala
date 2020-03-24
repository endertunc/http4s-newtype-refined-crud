package car.advert.quill

import cats.effect.IO

import doobie.syntax.connectionio.toConnectionIOOps

import car.advert.model.Id
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.quill.DoobiePostgresContext._
import io.getquill.{ idiom => _ }

object CarAdverts {

  private val carAdverts = quote {
    querySchema[CarAdvert](
      "car_advert",
      _.id -> "id",
      _.title -> "title",
      _.fuelType -> "fuelType",
      _.price -> "price",
      _.offerType -> "offerType",
      _.mileage -> "mileage",
      _.firstRegistration -> "firstRegistration"
    )
  }

  def findById(id: Id)(implicit xa: doobie.Transactor[IO]): IO[Option[CarAdvert]] =
    run(quote(carAdverts.filter(_.id == lift(id)))).map(_.headOption).transact(xa)

  def insert(carAdvert: CarAdvert)(implicit xa: doobie.Transactor[IO]): IO[CarAdvert] =
    run(quote(carAdverts.insert(lift(carAdvert)).returning(carAdvert => carAdvert))).transact(xa)

}
