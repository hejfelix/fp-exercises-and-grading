package exercises

object Matrix3x3 {
  def apply(rows: Array[Array[Double]]): Matrix3x3 = Matrix3x3(rows.map(_.toList).toList)
}

case class Matrix3x3(rows: List[List[Double]]) {
  assert(rows.length == 3, s"Matrix has wrong number of rows: ${rows.length} != 3")
  rows.foreach(row =>
    assert(row.length == 3, s"Matrix has wrong number of columns: ${row.length} != 3"))

  def pretty: String =
    rows.map(_.mkString("[", ",", "]")).mkString(",\n")

  def asArrays: Array[Array[Double]] = rows.map(_.toArray).toArray

  /**
    * We need this custom implementation since NaN != NaN...
    * */
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: Matrix3x3 =>
      (rows, that.rows).zipped.forall(
        (is, js) =>
          (is, js).zipped.forall(
            (x, y) => x.isNaN && y.isNaN || x == y
        ))
    case _ => false
  }
}
