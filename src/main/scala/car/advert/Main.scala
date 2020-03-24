package car.advert

import scala.concurrent.ExecutionContext

import org.http4s.server.blaze.BlazeServerBuilder

import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits._

import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

import com.colisweb.tracing.context.OpenTracingContext
import com.colisweb.tracing.core.TracingContextBuilder

import io.opentracing.Span
import io.opentracing.Tracer
import io.opentracing.util.GlobalTracer

import car.advert.config.AppConfig.AppConfig
import car.advert.migration.FlywayDatabaseMigrator
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.generic.auto._

// $COVERAGE-OFF$
object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      config                                      <- Resource.liftF(ConfigSource.default.loadOrThrow[AppConfig].pure[IO])
      txnEc                                       <- ExecutionContexts.cachedThreadPool[IO]
      connEc                                      <- ExecutionContexts.fixedThreadPool[IO](config.databaseConfig.connections.poolSize)
      databaseMigrator                            <- Resource.liftF(FlywayDatabaseMigrator[IO].pure[IO])
      implicit0(xa: Transactor[IO])               <- databaseMigrator.dbTransactor(config.databaseConfig, connEc, Blocker.liftExecutionContext(txnEc))
      implicit0(l: SelfAwareStructuredLogger[IO]) <- Resource.liftF(Slf4jLogger.create[IO])
      implicit0(tcb: TracingContextBuilder[IO])   <- Resource.liftF(OpenTracingContext.builder[IO, Tracer, Span](GlobalTracer.get()))
      ctx                                         <- Resource.liftF(new CarAdvertModule[IO]().pure[IO])
      _                                           <- Resource.liftF(databaseMigrator.migrate(xa))
      server <- BlazeServerBuilder[IO](ExecutionContext.Implicits.global) // think twice before using global EC on production
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(ctx.httpApp)
        .resource
    } yield server).use(_ => IO.never).as(ExitCode.Success)
}
// $COVERAGE-ON$
