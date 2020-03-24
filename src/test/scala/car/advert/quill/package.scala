package car.advert

import java.time.{ LocalDate, ZoneId }
import java.util.Date

import doobie.quill.DoobieContext

import car.advert.model.{ Id, Mileage, Price, Title }
import io.getquill.{ Literal, MappedEncoding }

package object quill {

  val DoobiePostgresContext = new DoobieContext.Postgres(Literal)

  implicit val instanceEncoder: MappedEncoding[LocalDate, Date] = MappedEncoding[LocalDate, Date](date => java.sql.Date.valueOf(date))
  implicit val instantDecoder: MappedEncoding[Date, LocalDate] =
    MappedEncoding[Date, LocalDate](date => date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate)

  implicit val idEncoder: MappedEncoding[Id, String] = MappedEncoding[Id, String](_.value.value)
  implicit val idDecoder: MappedEncoding[String, Id] = MappedEncoding[String, Id](Id.unsafeFrom)

  implicit val titleEncoder: MappedEncoding[Title, String] = MappedEncoding[Title, String](_.value.value)
  implicit val titleDecoder: MappedEncoding[String, Title] = MappedEncoding[String, Title](Title.unsafeFrom)

  implicit val priceEncoder: MappedEncoding[Price, Int] = MappedEncoding[Price, Int](_.value.value)
  implicit val priceDecoder: MappedEncoding[Int, Price] = MappedEncoding[Int, Price](Price.unsafeFrom)

  implicit val mileageEncoder: MappedEncoding[Mileage, Int] = MappedEncoding[Mileage, Int](_.value.value)
  implicit val mileageDecoder: MappedEncoding[Int, Mileage] = MappedEncoding[Int, Mileage](Mileage.unsafeFrom)

}
