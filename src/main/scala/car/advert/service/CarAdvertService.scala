package car.advert.service

import cats.effect.Async
import cats.effect.Sync
import cats.implicits._

import com.colisweb.tracing.core.TracingContext

import car.advert.TracingUtils._
import car.advert.http.Pagination
import car.advert.model.OrderByCriteria
import car.advert.model.entity.CarAdvert
import car.advert.model.error.AppError.NotFound
import car.advert.model.validation.CarAdvertValidator
import car.advert.model.{ Id, OrderByCriteria }
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

class CarAdvertService[F[_]: Async]()(implicit L: SelfAwareStructuredLogger[F], carAdvertRepositoryAlgebra: CarAdvertRepositoryAlgebra[F])
    extends CarAdvertValidator {

  def create(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert] =
    for {
      _                 <- L.trace(tc.ctx)(s"Validating car advert with id [${carAdvert.id} ] for create")
      _                 <- validateCarAdvert(carAdvert)
      _                 <- L.trace(tc.ctx)(s"Creating car advert with id [${carAdvert.id}]")
      insertedCarAdvert <- carAdvertRepositoryAlgebra.insert(carAdvert)
      _                 <- L.trace(tc.ctx)(s"Car advert with id [${carAdvert.id}] successfully created")
    } yield insertedCarAdvert

  def find(id: Id)(implicit tc: TracingContext[F]): F[CarAdvert] =
    for {
      _              <- L.trace(tc.ctx)(s"Retrieving car advert by id [$id]")
      maybeCarAdvert <- carAdvertRepositoryAlgebra.findById(id)
      _              <- L.warn(tc.ctx)(s"Car advert with id [$id] is not found").whenA(maybeCarAdvert.isEmpty)
      carAdvert      <- Sync[F].fromOption(maybeCarAdvert, NotFound(s"CarAdvert with id [$id] is not found"))
    } yield carAdvert

  def update(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert] =
    for {
      _                <- L.trace(tc.ctx)(s"Validating car advert with id [${carAdvert.id}] for update")
      _                <- validateCarAdvert(carAdvert)
      _                <- L.trace(tc.ctx)(s"Updating car advert with id [${carAdvert.id}]")
      updatedCarAdvert <- carAdvertRepositoryAlgebra.update(carAdvert)
      _                <- L.trace(tc.ctx)(s"Car advert with id [${carAdvert.id}] successfully updated")
    } yield updatedCarAdvert

  // //ToDo log
  //  def update(carAdvert: CarAdvert): F[CarAdvert] =
  //    OptionT(carAdvertRepositoryAlgebra.update(carAdvert))
  //      .getOrElseF(NotFound(s"CarAdvert with id [${carAdvert.id}] is not found").raiseError[F, CarAdvert])

  def delete(id: Id)(implicit tc: TracingContext[F]): F[Unit] =
    for {
      _ <- L.trace(tc.ctx)(s"Deleting car advert by id [$id]")
      _ <- carAdvertRepositoryAlgebra.deleteById(id)
    } yield ()

  def list(orderByCriteria: OrderByCriteria, pagination: Pagination)(implicit tc: TracingContext[F]): F[List[CarAdvert]] =
    carAdvertRepositoryAlgebra.list(orderByCriteria, pagination)

}

object CarAdvertService {
  def apply[F[_]: Async: SelfAwareStructuredLogger]()(implicit carAdvertRepositoryAlgebra: CarAdvertRepositoryAlgebra[F]): CarAdvertService[F] =
    new CarAdvertService()
}
