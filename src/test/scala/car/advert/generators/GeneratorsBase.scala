package car.advert.generators

import java.time.LocalDate

import scala.util.Random

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import io.estatico.newtype.Coercible

trait GeneratorsBase {

  implicit lazy val arbAlphaString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr.map(_.mkString))
  implicit lazy val arbPositiveInt: Arbitrary[Int]    = Arbitrary(Gen.chooseNum(1, Int.MaxValue))
  implicit lazy val arbNegativeInt: Gen[Int]          = Arbitrary(Gen.chooseNum(Int.MinValue, 0)).arbitrary
  //  implicit lazy val arbNegativeInt: Arbitrary[Int] = Arbitrary[Refined[Int, Less[_0]]]

  def randomPositiveInt(max: Int = Int.MaxValue): Int = Random.nextInt(max) + 1

  implicit val arbLocalDate: Arbitrary[LocalDate] = {
    val max = 100
    Arbitrary(
      LocalDate
        .now()
        .minusDays(randomPositiveInt(max))
        .minusWeeks(randomPositiveInt(max))
        .minusMonths(randomPositiveInt(max))
    )
  }

  implicit def newTypeArbitrary[R, N](implicit ev: Coercible[Arbitrary[R], Arbitrary[N]], R: Arbitrary[R]): Arbitrary[N] =
    ev(R)

}
