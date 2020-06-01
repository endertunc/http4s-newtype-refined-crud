package car.advert.http

import org.http4s.circe.CirceEntityDecoder
import org.http4s.circe.CirceEntityEncoder
import org.http4s.dsl.Http4sDsl

import cats.effect.Async
import cats.implicits._

import com.colisweb.tracing.core.TracingContext

import car.advert.model.error.AppError
import car.advert.model.error.AppError.InternalServerError
import car.advert.model.error.Error_OUT
import io.chrisdavenport.log4cats.Logger

abstract class BaseHttp4sRoute[F[_]: Async: Logger] extends Http4sDsl[F] with CirceEntityEncoder with CirceEntityDecoder {

  implicit class FToResponse[A](result: F[A]) {
    private def exceptionToErrorOut(t: Throwable)(implicit tracingContext: TracingContext[F]): F[Error_OUT] =
      t match {
        case appError: AppError => appError.toErrorOut.pure[F]
        case _ =>
          Logger[F].error(t)(message = "Unexpected exception") *>
          new InternalServerError("Unexpected exception").toErrorOut.pure[F]
      }

    def toResponse(implicit tracingContext: TracingContext[F]): F[Either[Error_OUT, A]] =
      result.map(t => t.asRight[Error_OUT]).recoverWith {
        case t: Throwable => exceptionToErrorOut(t).map(_.asLeft[A])
      }
  }

}
