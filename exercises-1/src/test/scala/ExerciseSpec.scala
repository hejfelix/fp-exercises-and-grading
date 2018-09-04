import Jama.{Matrix => JamaMatrix}
import exercises.ExerciseImplementation.MonoidInstances.productMonoidFromTranspose
import exercises.{ExerciseImplementation, Matrix3x3}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

/**
  * We'll use Jama, an established Java Matrix library, to test our matrix operations
  * against. While this is a 'happy accident' that the problem has already been solved well
  * enough to have a baseline for our tests, it serves well for the purpose of testing against
  * a black box without giving away the solution.
  */
class ExerciseSpec
    extends WordSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with Matrix3x3Abitrary {

  val solution = new ExerciseImplementation

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(1000)

  "A matrix plus zero equals itself" in {
    forAll { matrix: Matrix3x3 =>
      import exercises.ExerciseImplementation.MonoidInstances.sumMonoid
      solution.sum(matrix, sumMonoid.empty) shouldBe matrix
    }
  }

  "Transpose matrices" in {
    forAll { matrix: Matrix3x3 =>
      val jamaMatrix = JamaMatrix.constructWithCopy(matrix.asArrays)
      val expected   = Matrix3x3(jamaMatrix.transpose().getArray)
      solution.transpose(matrix) shouldBe expected
    }
  }

  "A matrix multiplied by the identity matrix equals itself" in {
    forAll { matrix: Matrix3x3 =>
      implicit val productMonoid = productMonoidFromTranspose(solution.transpose)
      solution.product(matrix, productMonoid.empty) shouldBe matrix
    }
  }

  "Sum matrices" in {
    forAll { matrices: List[Matrix3x3] =>
      import exercises.ExerciseImplementation.MonoidInstances.sumMonoid
      val result: Matrix3x3 = solution.sum(matrices: _*) //spread list out into varargs

      val jamaMatrices: List[JamaMatrix] =
        matrices
          .map(m => m.rows.map(_.toArray).toArray)
          .map(JamaMatrix.constructWithCopy)

      val jamaZero: JamaMatrix =
        JamaMatrix.constructWithCopy(sumMonoid.empty.asArrays)

      val expected: JamaMatrix = jamaMatrices.fold(jamaZero)(_ plus _)

      result.asArrays shouldBe expected.getArray
    }
  }

  "Multiply matrices" in {
    forAll { matrices: List[Matrix3x3] =>
      implicit val productMonoid = productMonoidFromTranspose(solution.transpose)
      val result: Matrix3x3      = solution.product(matrices: _*) //spread list out into varargs

      val jamaMatrices: List[JamaMatrix] =
        matrices
          .map(m => m.rows.map(_.toArray).toArray)
          .map(JamaMatrix.constructWithCopy)

      val jamaZero: JamaMatrix =
        JamaMatrix.constructWithCopy(productMonoid.empty.asArrays)

      val expected: JamaMatrix = jamaMatrices.fold(jamaZero)(_ times _)

      result shouldBe Matrix3x3(expected.getArray)
    }

  }

}
