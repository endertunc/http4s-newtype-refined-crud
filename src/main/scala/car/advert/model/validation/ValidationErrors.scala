package car.advert.model.validation

import scala.util.control.NoStackTrace

trait ValidationErrors extends NoStackTrace {
  def message: String
}
