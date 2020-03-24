package car.advert.repository.algebra

import com.colisweb.tracing.core.TracingContext

import car.advert.http.Pagination
import car.advert.model.entity.CarAdvert
import car.advert.model.{ Id, OrderByCriteria }

trait CarAdvertRepositoryAlgebra[F[_]] {

  def findById(id: Id)(implicit tc: TracingContext[F]): F[Option[CarAdvert]]
  def insert(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert]
  def update(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert]
  def deleteById(id: Id)(implicit tc: TracingContext[F]): F[Unit]
  def list(orderByCriteria: OrderByCriteria, pagination: Pagination)(implicit tc: TracingContext[F]): F[List[CarAdvert]]

}
