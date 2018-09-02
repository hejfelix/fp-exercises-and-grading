import exercises.Matrix3x3
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbDouble

trait Matrix3x3Abitrary {
  implicit def arbMatrix3x3: Arbitrary[Matrix3x3] = Arbitrary {
    Gen
      .listOfN(3, Gen.listOfN(3, arbDouble.arbitrary.filter(d => !d.isNaN)))
      .map(Matrix3x3.apply)
  }
}
