package exercises

import cats.Monoid

trait Exercise {
  def sum(matrices: Matrix3x3*)(implicit m: Monoid[Matrix3x3]): Matrix3x3
  def transpose(matrix: Matrix3x3): Matrix3x3
  def product(matrices: Matrix3x3*)(implicit m: Monoid[Matrix3x3]): Matrix3x3
}
