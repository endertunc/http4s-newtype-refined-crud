package car.advert

import scala.concurrent.duration._

import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.{ CORSConfig, CORS, Logger => RequestResponseLogger }
import org.http4s.{ HttpApp, HttpRoutes, Response }

import cats.data.Kleisli._
import cats.data.{ Kleisli, OptionT }
import cats.effect.{ ConcurrentEffect, ContextShift }
import cats.implicits._

import doobie.util.transactor.Transactor

import com.colisweb.tracing.core.TracingContextBuilder

import car.advert.http.CarAdvertRoutes
import car.advert.http.CarAdvertRoutes
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import car.advert.repository.algebra.CarAdvertRepositoryAlgebra
import car.advert.repository.postgres.doobie.DoobiePgCarAdvertRepository
import car.advert.repository.postgres.doobie.DoobiePgCarAdvertRepository
import car.advert.service.CarAdvertService
import car.advert.service.CarAdvertService
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

class CarAdvertModule[F[_]: ConcurrentEffect: ContextShift: Transactor: SelfAwareStructuredLogger: TracingContextBuilder]() {

  implicit val carAdvertRepository: CarAdvertRepositoryAlgebra[F] = DoobiePgCarAdvertRepository()
  implicit val carAdvertService: CarAdvertService[F]              = CarAdvertService()
  implicit val carAdvertRoutes: CarAdvertRoutes[F]                = CarAdvertRoutes()

  def httpApp: HttpApp[F] =
    CORS(endpoints, CORSConfig(anyOrigin = true, anyMethod = true, allowCredentials = true, maxAge = 1.day.toSeconds)).orNotFound

  def endpoints: HttpRoutes[F] =
    // The main logger should be adjusted via env variables.
    RequestResponseLogger.httpRoutes(logHeaders = true, logBody = true)(Router("/" -> carAdvertRoutes.routes)) <+>
    RequestResponseLogger.httpRoutes(logHeaders = true, logBody = false)(Kleisli(_ => OptionT.pure(Response.notFound))) // log all NotFounds

}
