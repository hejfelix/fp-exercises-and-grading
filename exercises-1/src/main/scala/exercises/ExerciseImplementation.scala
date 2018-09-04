package exercises

import cats.Monoid
import cats.instances.list._
import cats.syntax.foldable._

object ExerciseImplementation {

  object MonoidInstances {

    type Scalar = Double
    type Vector = List[Scalar]

    implicit val sumMonoid = new Monoid[Matrix3x3] {
      override def empty: Matrix3x3 = Matrix3x3(List.tabulate(3, 3)((_, _) => 0d))

      override def combine(x: Matrix3x3, y: Matrix3x3): Matrix3x3 =
        Matrix3x3(
          (x.rows, y.rows).zipped
            .map((x, y) => (x, y).zipped.map(_ + _)))
    }

    def productMonoidFromTranspose(transpose: Matrix3x3 => Matrix3x3): Monoid[Matrix3x3] =
      new Monoid[Matrix3x3] {

        def dotProduct(a: Vector, b: Vector): Scalar = (a, b).zipped.map(_ * _).sum

        /**
          * [1, 0, 0]
          * [0, 1, 0]
          * [0, 0, 1]
          */
        override def empty: Matrix3x3 = Matrix3x3(
          List.tabulate(3, 3)((i, j) => if (i == j) 1d else 0d)
        )

        override def combine(x: Matrix3x3, y: Matrix3x3): Matrix3x3 = {
          val a: List[List[Scalar]]           = x.rows
          val bTransposed: List[List[Scalar]] = transpose(y).rows
          Matrix3x3(a.map(aRow => bTransposed.map(bCol => dotProduct(aRow, bCol))))
        }

      }

  }

}

class ExerciseImplementation extends Exercise {

  def transposeLists(rows: List[List[Double]]): List[List[Double]] = rows match {
    case xs if xs.forall(_.isEmpty) => Nil
    case rows                       => rows.map(_.head) :: transposeLists(rows.map(_.tail))
  }
//  def transposeLists(list: List[List[Double]]): List[List[Double]] = list

  override def sum(matrices: Matrix3x3*)(implicit m: Monoid[Matrix3x3]): Matrix3x3 =
    matrices.toList.combineAll
  override def transpose(matrix: Matrix3x3): Matrix3x3 =
    Matrix3x3(transposeLists(matrix.rows))
  override def product(matrices: Matrix3x3*)(implicit m: Monoid[Matrix3x3]): Matrix3x3 =
    matrices.toList.combineAll
}
