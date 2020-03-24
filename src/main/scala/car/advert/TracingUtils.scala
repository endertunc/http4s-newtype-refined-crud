package car.advert

import com.colisweb.tracing.core.TracingContext

object TracingUtils {

  implicit class RichTracingContext[F[_]](tracingContext: TracingContext[F]) {
    def ctx: Map[String, String] = Map("correlationId" -> tracingContext.correlationId)
  }

}
