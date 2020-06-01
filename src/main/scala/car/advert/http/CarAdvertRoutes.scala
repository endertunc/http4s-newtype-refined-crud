package car.advert.http

import org.http4s._
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir._
import sttp.tapir.codec.enumeratum._
import sttp.tapir.codec.refined._
import sttp.tapir.endpoint
import sttp.tapir.json.circe._

import cats.data.NonEmptyList
import cats.effect.{ Async, ContextShift }
import cats.implicits._

import io.circe.generic.auto._

import com.colisweb.tracing.core.TracingContext
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.http.server._

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid

import car.advert.model.RefinedId
import car.advert.model.entity.CarAdvert
import car.advert.model.error.Error_OUT
import car.advert.model.error.Error_OUT.InvalidFieldResponse
import car.advert.model.error.Error_OUT.SimpleErrorResponse
import car.advert.model.request.CarAdvert_IN
import car.advert.model.response.DeleteCarAdvert_OUT
import car.advert.model.validation.CarAdvertInValidator
import car.advert.model.{ Id, OrderByCriteria }
import car.advert.service.CarAdvertService
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

class CarAdvertRoutes[F[_]: Async: ContextShift: TracingContextBuilder]()(
    implicit L: SelfAwareStructuredLogger[F],
    carAdvertService: CarAdvertService[F]
) extends BaseHttp4sRoute[F]
    with PaginationValidator
    with CarAdvertInValidator {
  private val apiContext = "advert"

  // ToDo improve API definitions by providing desc and examples etc.
  val getCarAdvertEndpoint: Endpoint[Refined[String, Uuid], Error_OUT, CarAdvert, Nothing] =
    endpoint.get
      .errorOut(
        oneOf[Error_OUT](
          statusMapping(StatusCode.NotFound, jsonBody[SimpleErrorResponse])
        )
      )
      .in(apiContext / path[RefinedId]("id"))
      .out(jsonBody[CarAdvert])

  val createCarAdvertEndpoint: Endpoint[CarAdvert_IN, Error_OUT, CarAdvert, Nothing] =
    endpoint.post
      .errorOut(
        oneOf[Error_OUT](
          statusMapping(StatusCode.NotFound, jsonBody[SimpleErrorResponse])
        )
      )
      .in(apiContext)
      .in(jsonBody[CarAdvert_IN])
      .out(jsonBody[CarAdvert])

  val updateCarAdvertEndpoint: Endpoint[CarAdvert_IN, Error_OUT, CarAdvert, Nothing] =
    endpoint.put
      .errorOut(
        oneOf[Error_OUT](
          statusMapping(StatusCode.NotFound, jsonBody[SimpleErrorResponse]),
          statusMapping(StatusCode.BadRequest, jsonBody[InvalidFieldResponse])
        )
      )
      .in(apiContext)
      .in(jsonBody[CarAdvert_IN])
      .out(jsonBody[CarAdvert])

  val deleteCarAdvertEndpoint: Endpoint[Refined[String, Uuid], Error_OUT, DeleteCarAdvert_OUT, Nothing] =
    endpoint.delete
      .errorOut(
        oneOf[Error_OUT](
          statusMapping(StatusCode.NotFound, jsonBody[SimpleErrorResponse]),
          statusMapping(StatusCode.BadRequest, jsonBody[InvalidFieldResponse])
        )
      )
      .in(apiContext / path[RefinedId]("id"))
      .out(jsonBody[DeleteCarAdvert_OUT])

  val listCarAdvertEndpoint: Endpoint[(Option[OrderByCriteria], Option[Long], Option[Long]), Error_OUT, List[CarAdvert], Nothing] =
    endpoint.get
      .errorOut(
        oneOf[Error_OUT](
          statusMapping(StatusCode.NotFound, jsonBody[SimpleErrorResponse]),
          statusMapping(StatusCode.BadRequest, jsonBody[InvalidFieldResponse])
        )
      )
      .in(apiContext / "list")
      .in(query[Option[OrderByCriteria]]("orderBy"))
      .in(query[Option[Long]]("offset"))
      .in(query[Option[Long]]("limit"))
      .out(jsonBody[List[CarAdvert]])

  val getCarAdvertRoute: HttpRoutes[F] = getCarAdvertEndpoint.toTracedRoute[F] {
    case (refinedId, implicit0(tracingContext: TracingContext[F])) =>
      find(Id(refinedId)).toResponse
  }

  val createCarAdvertRoute: HttpRoutes[F] = createCarAdvertEndpoint.toTracedRoute[F] {
    case (carAdvertIn, implicit0(tracingContext: TracingContext[F])) =>
      create(carAdvertIn).toResponse
  }

  val updateCarAdvertRoute: HttpRoutes[F] = updateCarAdvertEndpoint.toTracedRoute[F] {
    case (carAdvertIn, implicit0(tracingContext: TracingContext[F])) =>
      update(carAdvertIn).toResponse
  }

  val deleteCarAdvertRoute: HttpRoutes[F] = deleteCarAdvertEndpoint.toTracedRoute[F] {
    case (refinedId, implicit0(tracingContext: TracingContext[F])) =>
      delete(Id(refinedId)).map(_ => DeleteCarAdvert_OUT()).toResponse
  }

  val listCarAdvertRoute: HttpRoutes[F] = listCarAdvertEndpoint.toTracedRoute[F] {
    case ((maybeCriteria, maybeOffset, maybeLimit), implicit0(tracingContext: TracingContext[F])) =>
      list(maybeCriteria, maybeOffset, maybeLimit).toResponse
  }

  def find(id: Id)(implicit tracingContext: TracingContext[F]): F[CarAdvert] = carAdvertService.find(id)
  def delete(id: Id)(implicit tracingContext: TracingContext[F]): F[Unit]    = carAdvertService.delete(id)
  def create(carAdvertIn: CarAdvert_IN)(implicit tracingContext: TracingContext[F]): F[CarAdvert] =
    for {
      carAdvert        <- validateCarAdvertIn(carAdvertIn)
      createdCarAdvert <- carAdvertService.create(carAdvert)
    } yield createdCarAdvert
  def update(carAdvertIn: CarAdvert_IN)(implicit tracingContext: TracingContext[F]): F[CarAdvert] =
    for {
      carAdvert        <- validateCarAdvertIn(carAdvertIn)
      updatedCarAdvert <- carAdvertService.update(carAdvert)
    } yield updatedCarAdvert

  def list(maybeCriteria: Option[OrderByCriteria], maybeOffset: Option[Long], maybeLimit: Option[Long])(
      implicit tracingContext: TracingContext[F]
  ): F[List[CarAdvert]] = {
    val criteria = maybeCriteria.getOrElse(OrderByCriteria.Id)
    for {
      pagination <- Pagination.fromParams(maybeOffset, maybeLimit)
      carAdverts <- carAdvertService.list(criteria, pagination)
    } yield carAdverts
  }

  val routes: HttpRoutes[F] =
    listCarAdvertRoute <+>
    getCarAdvertRoute <+>
    createCarAdvertRoute <+>
    deleteCarAdvertRoute <+>
    updateCarAdvertRoute

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        getCarAdvertEndpoint,
        createCarAdvertEndpoint,
        updateCarAdvertEndpoint,
        deleteCarAdvertEndpoint,
        listCarAdvertEndpoint
      )

}

object CarAdvertRoutes {

  object IdPathVariable { def unapply(id: String): Option[Id] = Id.from(id).toOption }
  object CriteriaMatcher extends OptionalQueryParamDecoderMatcher[OrderByCriteria]("orderBy")

  implicit val criteriaQueryParamDecoder: QueryParamDecoder[OrderByCriteria] = QueryParamDecoder[String].map(OrderByCriteria.withNameInsensitive)

  def apply[F[_]: Async: SelfAwareStructuredLogger: CarAdvertService: ContextShift: TracingContextBuilder](): CarAdvertRoutes[F] =
    new CarAdvertRoutes[F]()
}
