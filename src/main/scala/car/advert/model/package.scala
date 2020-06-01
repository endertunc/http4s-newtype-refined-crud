package car.advert

import sttp.tapir.Codec
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.CodecFormat
import sttp.tapir.Schema
import sttp.tapir.codec.refined._
import sttp.tapir.json.circe._

import io.circe.Decoder
import io.circe.Encoder
import io.circe.refined._

import doobie.refined.implicits._
import doobie.util.meta.Meta

import eu.timepit.refined.api.{ Refined, RefinedTypeOps }
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.string.{ Trimmed, Uuid }
import eu.timepit.refined.types.numeric.PosInt

import io.estatico.newtype.macros.newtype

// Scoverage can't figure out @newtype
// $COVERAGE-OFF$
package object model {

  type RefinedTitle = String Refined (Trimmed And NonEmpty)
  object RefinedTitle extends RefinedTypeOps[RefinedTitle, String]
  @newtype
  @deriving(Encoder, Decoder, Meta)
  case class Title(value: RefinedTitle)

  object Title {
    implicit val titleCodec: Codec[Title, CodecFormat.Json, String] = implicitly[JsonCodec[RefinedTitle]].map(Title(_))(_.value)
    implicit val titleSchema: Schema[Title]                         = titleCodec.meta.schema

    def from(title: String): Either[String, Title] = RefinedTitle.from(title).map(Title(_))
    def unsafeFrom(title: String): Title           = Title(RefinedTitle.unsafeFrom(title))
  }

  type RefinedPrice = Int Refined Positive
  object RefinedPrice extends RefinedTypeOps[RefinedPrice, Int]
  @newtype
  @deriving(Encoder, Decoder, doobie.Meta)
  case class Price(value: PosInt)

  object Price {
    implicit val priceCodec: Codec[Price, CodecFormat.Json, String] = implicitly[JsonCodec[RefinedPrice]].map(Price(_))(_.value)
    implicit val priceSchema: Schema[Price]                         = priceCodec.meta.schema

    def from(price: Int): Either[String, Price] = RefinedPrice.from(price).map(Price(_))
    def unsafeFrom(price: Int): Price           = Price(RefinedPrice.unsafeFrom(price))
  }

  type RefinedMileage = Int Refined Positive
  object RefinedMileage extends RefinedTypeOps[RefinedMileage, Int]
  @newtype
  @deriving(Encoder, Decoder, doobie.Meta)
  case class Mileage(value: RefinedMileage)

  object Mileage {
    implicit val mileageCodec: Codec[Mileage, CodecFormat.Json, String] = implicitly[JsonCodec[RefinedMileage]].map(Mileage(_))(_.value)
    implicit val mileageSchema: Schema[Mileage]                         = mileageCodec.meta.schema

    def from(mileage: Int): Either[String, Mileage] = RefinedMileage.from(mileage).map(Mileage(_))
    def unsafeFrom(mileage: Int): Mileage           = Mileage(RefinedMileage.unsafeFrom(mileage))
  }

  type RefinedId = String Refined Uuid
  object RefinedId extends RefinedTypeOps[RefinedId, String]
  @newtype
  @deriving(Encoder, Decoder, doobie.Meta)
  case class Id(value: RefinedId)

  object Id {
    implicit val idCodec: Codec[Id, CodecFormat.Json, String] = implicitly[JsonCodec[RefinedId]].map(Id(_))(_.value)
    implicit val IdSchema: Schema[Id]                         = idCodec.meta.schema

    def from(id: String): Either[String, Id] = RefinedId.from(id).map(Id(_))
    def unsafeFrom(id: String): Id           = Id(RefinedId.unsafeFrom(id))
  }

}

// $COVERAGE-ON$
