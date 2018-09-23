package com.example
import cats.effect.Sync

import scala.concurrent.duration._
import scala.xml.{Node, NodeSeq, XML}
class TestReportParser[F[_]](implicit F: Sync[F]) {

  def parse(testReport: String): F[List[TestResult]] = F.delay {
    val xml            = XML.loadString(testReport)
    val cases: NodeSeq = xml \\ "testcase"

    cases.theSeq.map(caseToResult).toList
  }

  private def hasChildren(node: Node): Boolean =
    node.nonEmptyChildren.filterNot(_.toString.trim.isEmpty).nonEmpty

  private def caseToResult(node: Node): TestResult =
    if (hasChildren(node))
      Failed((node \ "failure") \@ "message", (node \@ "time").toDouble.seconds, node \@ "name")
    else
      Succeeded((node \@ "time").toDouble.seconds, node \@ "name")

}
