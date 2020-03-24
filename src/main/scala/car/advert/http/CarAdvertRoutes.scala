package car.advert.http

import org.http4s._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

import cats.effect.{ Async, ContextShift }
import cats.implicits._

import com.colisweb.tracing.core.TracingContext
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.http.server.TracedHttpRoutes
import com.colisweb.tracing.http.server.TracedHttpRoutes.using

import car.advert.http.CarAdvertRoutes.{ CriteriaMatcher, IdPathVariable }
import car.advert.http.Pagination._
import car.advert.model.OrderByCriteria
import car.advert.model.entity.CarAdvert
import car.advert.model.entity.CarAdvert
import car.advert.model.request.CarAdvert_IN
import car.advert.model.request.CarAdvert_IN
import car.advert.model.validation.CarAdvertInValidator
import car.advert.model.validation.CarAdvertInValidator
import car.advert.model.{ Id, OrderByCriteria }
import car.advert.service.CarAdvertService
import car.advert.service.CarAdvertService
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

class CarAdvertRoutes[F[_]: Async: ContextShift: TracingContextBuilder]()(
    implicit L: SelfAwareStructuredLogger[F],
    carAdvertService: CarAdvertService[F]
) extends BaseHttp4sRoute[F]
    with PaginationValidator
    with CarAdvertInValidator {

  val routes: HttpRoutes[F] = TracedHttpRoutes {

    case GET -> Root / IdPathVariable(id) using implicit0(tracingContext: TracingContext[F]) => carAdvertService.find(id).toResponse

    case GET -> Root
        :? CriteriaMatcher(validatedCriteria)
        +& OffsetMatcher(maybeOffset)
        +& LimitMatcher(maybeLimit)
        using implicit0(tracingContext: TracingContext[F]) => {

      val criteria = validatedCriteria.getOrElse(OrderByCriteria.Id)

      val result: F[List[CarAdvert]] = for {
        pagination <- Pagination.fromParams(maybeOffset, maybeLimit)
        carAdverts <- carAdvertService.list(criteria, pagination)
      } yield carAdverts
      result.toResponse
    }

    case req @ POST -> Root using implicit0(tracingContext: TracingContext[F]) =>
      req.request.decode[CarAdvert_IN] { carAdvertIn =>
        val result = for {
          carAdvert        <- validateCarAdvertIn(carAdvertIn)
          createdCarAdvert <- carAdvertService.create(carAdvert)
        } yield createdCarAdvert
        result.toResponse
      }

    case req @ PUT -> Root using implicit0(tracingContext: TracingContext[F]) =>
      req.request.decode[CarAdvert_IN] { carAdvertIn =>
        val result = for {
          carAdvert        <- validateCarAdvertIn(carAdvertIn)
          updatedCarAdvert <- carAdvertService.update(carAdvert)
        } yield updatedCarAdvert
        result.toResponse
      }

    case DELETE -> Root / IdPathVariable(id) using implicit0(tracingContext: TracingContext[F]) => carAdvertService.delete(id).toResponse

    //    case req @ POST -> Root using tracingContext =>
    //      req.request.asJsonDecode[CarAdvert].attempt.flatMap {
    //        case Left(e)          => UnprocessableEntity(e.getMessage)
    //        case Right(carAdvert) => carAdvertService.create(carAdvert).flatMap(Ok(_))
    //      }

  }

}

object CarAdvertRoutes {

  object IdPathVariable { def unapply(id: String): Option[Id] = Id.from(id).toOption }
  object CriteriaMatcher extends OptionalQueryParamDecoderMatcher[OrderByCriteria]("orderBy")

  implicit val criteriaQueryParamDecoder: QueryParamDecoder[OrderByCriteria] = QueryParamDecoder[String].map(OrderByCriteria.withNameInsensitive)

  def apply[F[_]: Async: SelfAwareStructuredLogger: CarAdvertService: ContextShift: TracingContextBuilder](): CarAdvertRoutes[F] =
    new CarAdvertRoutes[F]()
}
