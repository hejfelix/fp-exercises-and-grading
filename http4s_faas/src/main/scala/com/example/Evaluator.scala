package com.example
import better.files.File
import cats.effect.Sync
import cats.implicits._

import scala.sys.process.Process
sealed trait EvaluationResult {
  def testReport: List[TestResult]
}
case class Success(s: String, testReport: List[TestResult])      extends EvaluationResult
case class Failure(exitCode: Long, testReport: List[TestResult]) extends EvaluationResult

class Evaluator[F[_]](reportParser: TestReportParser[F])(implicit F: Sync[F]) {

  private val logger = org.log4s.getLogger

  def evaluate(extractedDir: File): F[EvaluationResult] =
    (runExercise(extractedDir) <* deleteFolder(extractedDir))
      .flatMap {
        case (exitCode, testReport) =>
          reportParser
            .parse(testReport)
            .map(results => (exitCode, results))
      }
      .map {
        case (0, testResults) =>
          logger.info(s"Evaluation success!")
          Success("", testResults)
        case (exitCode, testResults) =>
          logger.info(s"Evaluation succeeded with failure status: ${exitCode}!")
          Failure(exitCode, testResults)
      }

  private def deleteFolder(exerciseFolder: File): F[Unit] = F.unit

  def runExercise(exerciseFolder: File): F[(Int, String)] =
    F.delay {
      logger.info(s"Running exercise in folder: ${exerciseFolder.pathAsString}")
      val shellCommand = "sbt exercises1/test"
      logger.info(s"Shelling out: \n${exerciseFolder.pathAsString}> $shellCommand")
      val exitCode = Process(shellCommand, exerciseFolder.toJava).!
      (exitCode, getReport(exerciseFolder))
    }

  private def getReport(exerciseFolder: File): String =
    (exerciseFolder / "exercises-1/target/test-reports/ExerciseSpec.xml").contentAsString

}
