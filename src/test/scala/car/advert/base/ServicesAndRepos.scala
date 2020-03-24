package car.advert.base

import scala.concurrent.Future

import org.http4s.EntityBody

import cats.effect.IO

import io.circe.Encoder
import io.circe._
import io.circe.syntax._

import com.colisweb.tracing.context.OpenTracingContext
import com.colisweb.tracing.core.TracingContextBuilder

import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer

import org.scalatest.Assertion
import org.scalatest.Suite

import car.advert.CarAdvertModule
import fs2.Stream
import fs2.text.utf8Encode
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalactic.source.Position

trait ServicesAndRepos extends TestEmbeddedPostgres { self: Suite =>

  implicit def toPureIO[T](statement: T): IO[T]                 = IO.pure(statement)
  implicit def toJsonBody[T: Encoder](model: T): EntityBody[IO] = Stream(model.asJson.noSpaces).through(utf8Encode)

  // ToDo move somewhere else
  def ioSuit[A](testBlock: => IO[Assertion])(implicit pos: Position): Future[Assertion] = testBlock.unsafeToFuture()

  // ToDo move somewhere else
  def failedIOSuit[A](testCode: => IO[Assertion])(onError: Throwable => Assertion): Assertion =
    testCode
      .handleErrorWith(appError => IO(onError(appError)))
      .unsafeRunSync()

  implicit lazy val logger: SelfAwareStructuredLogger[IO]            = Slf4jLogger.create[IO].unsafeRunSync
  implicit lazy val tracingContextBuilder: TracingContextBuilder[IO] = OpenTracingContext.builder[IO, Tracer, Span](GlobalTracer.get()).unsafeRunSync
  val ctx: CarAdvertModule[IO]                                       = new CarAdvertModule[IO]()

}
