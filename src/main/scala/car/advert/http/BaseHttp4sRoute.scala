package car.advert.http

import org.http4s.Response
import org.http4s.circe.CirceEntityDecoder
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.Http4sDsl

import cats.effect.Async
import cats.implicits._

import io.circe.Encoder
import io.circe.syntax._

import car.advert.model.error.AppError
import io.chrisdavenport.log4cats.Logger

abstract class BaseHttp4sRoute[F[_]: Async: Logger] extends Http4sDsl[F] with CirceEntityEncoder with CirceEntityDecoder {

  implicit class FToResponse[A](result: F[A]) {
    def toResponse(implicit encoder: Encoder[A]): F[Response[F]] =
      result
        .flatMap {
          case _: Unit => Ok()
          case model   => Ok(model.asJson)
        }
        .recoverWith {
          case appError: AppError => appError.toHttpResponse()
          case t: Exception =>
            println(t)
            Logger[F].error(t)(message = "Unexpected exception") *> InternalServerError("Internal Server Error")
        }
  }

}
