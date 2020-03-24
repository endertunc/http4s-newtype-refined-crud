package car.advert.repository.postgres.doobie

import cats.effect.{ Async, Sync }
import cats.implicits._

import doobie._
import doobie.implicits._
import doobie.implicits.legacy.localdate.JavaTimeLocalDateMeta
import doobie.refined.implicits._

import com.colisweb.tracing.core.TracingContext

import car.advert.TracingUtils._
import car.advert.http.Pagination
import car.advert.model.Instances._
import car.advert.model.OrderByCriteria
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.model.error.AppError.NotFound
import car.advert.model.{ Id, OrderByCriteria }
import car.advert.repository.SqlPaginationSupport
import car.advert.repository.SqlPaginationSupport
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

class DoobiePgCarAdvertRepository[F[_]: Async]()(implicit L: SelfAwareStructuredLogger[F], xa: Transactor[F]) extends CarAdvertRepositoryAlgebra[F] {

  override def insert(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert] =
    tc.span("insert") use { _ => CarAdvertStatements.insert(carAdvert).unique.transact(xa) }

  override def findById(id: Id)(implicit tc: TracingContext[F]): F[Option[CarAdvert]] =
    tc.span("find") use { _ => CarAdvertStatements.find(id).option.transact(xa) }

  override def update(carAdvert: CarAdvert)(implicit tc: TracingContext[F]): F[CarAdvert] =
    tc.span("update") use { _ =>
      for {
        maybeCarAdvert <- CarAdvertStatements.update(carAdvert).option.transact(xa)
        _              <- L.warn(s"Car advert with id [${carAdvert.id}] is not found").whenA(maybeCarAdvert.isEmpty)
        carAdvert      <- Sync[F].fromOption(maybeCarAdvert, NotFound(s"Car advert with id [${carAdvert.id}] is not found"))
      } yield carAdvert
    }

  override def deleteById(id: Id)(implicit tc: TracingContext[F]): F[Unit] =
    tc.span("delete") use { _ =>
      CarAdvertStatements
        .delete(id)
        .run
        .transact(xa)
        .flatMap {
          case effectedRows: Int if effectedRows == 0 =>
            L.warn(tc.ctx)(s"Car advert with id [$id] is not found") *>
            NotFound(s"Car advert with id [$id] is not found").raiseError[F, Unit]
          case effectedRows: Int if effectedRows == 1 => Sync[F].delay(())
        }
    }

  override def list(orderByCriteria: OrderByCriteria, pagination: Pagination)(implicit tc: TracingContext[F]): F[List[CarAdvert]] =
    tc.span("insert") use { _ =>
      CarAdvertStatements
        .list(orderByCriteria, pagination)
        .to[List]
        .transact(xa)
    }
}

object DoobiePgCarAdvertRepository {
  def apply[F[_]: Async: SelfAwareStructuredLogger: Transactor](): CarAdvertRepositoryAlgebra[F] = new DoobiePgCarAdvertRepository()
}

object CarAdvertStatements extends SqlPaginationSupport {

  val table: Fragment     = fr"car_advert"
  val fields: Seq[String] = Seq("id", "title", "price", "offerType", "fuelType", "mileage", "firstRegistration")
  val columns: Fragment   = fields.toList.map(Fragment.const(_)).intercalate(fr",")
  val keys: Fragment      = fields.toList.map(Fragment.const(_)).foldSmash(fr"(", fr",", fr")")
  val returning: Fragment = fr"RETURNING" ++ columns

  def insert(carAdvert: CarAdvert): doobie.Query0[CarAdvert] = {
    import carAdvert._
    (fr"INSERT INTO " ++ table ++ fr"VALUES ($id, $title, $price, $offerType, $fuelType, $mileage, $firstRegistration)" ++
    returning).query[CarAdvert]
  }

  def find(id: Id): doobie.Query0[CarAdvert] =
    (fr"SELECT" ++ columns ++ fr"FROM" ++ table ++ fr"WHERE id = $id").query[CarAdvert]

  def update(carAdvert: CarAdvert): doobie.Query0[CarAdvert] = {
    import carAdvert._
    (fr"UPDATE" ++ table ++ fr"""SET title = $title, fuelType = $fuelType, price = $price,
                                |offerType = $offerType, mileage = $mileage, firstRegistration = $firstRegistration
                                |WHERE id = $id""".stripMargin ++ returning).query[CarAdvert]
  }

  def delete(id: Id): doobie.Update0 = (fr"DELETE FROM" ++ table ++ fr"WHERE id = $id").update

  def list(orderByCriteria: OrderByCriteria, pagination: Pagination): doobie.Query0[CarAdvert] = {
    val fragment: Fragment = fr"SELECT" ++ columns ++ fr"FROM" ++ table ++ fr"ORDER BY" ++ Fragment.const(orderByCriteria.entryName)
    paginate(pagination)(fragment)
  }

  //  val table = fr"url"
  //  val fields = Seq("id", "url", "createdAt")
  //  val columns = fields.toList.map(Fragment.const(_)).intercalate(fr",")
  //  val keys = fields.toList.map(Fragment.const(_)).foldSmash(fr"(", fr",", fr")")
  //
  //  def fetch(id: Id): Query0[UrlEntity] =
  //    (fr"select" ++ columns ++ fr"from" ++ table ++ fr"where id = $id").query[UrlEntity]

}
