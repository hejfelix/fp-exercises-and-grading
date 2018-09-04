package grading.docker
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

import better.files.File
import cats.effect.Sync
import cats.implicits._
import com.google.common.io.CharStreams
import com.spotify.docker.client.messages._
import com.spotify.docker.client.{DockerClient, LogStream, ProgressHandler}
import grading.email.{TestReportParser, TestResult}
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

sealed trait EvaluationResult {
  def testReport: List[TestResult]
}
case class Success(s: String, testReport: List[TestResult])      extends EvaluationResult
case class Failure(exitCode: Long, testReport: List[TestResult]) extends EvaluationResult

class Evaluator[F[_]](dockerClient: DockerClient,
                      dockerfileName: String,
                      reportParser: TestReportParser[F])(implicit F: Sync[F]) {

  private val logger = org.log4s.getLogger

  def evaluate(extractedDir: File): F[EvaluationResult] = build(extractedDir) >>= {
    case (imageName, _) =>
      (runContainer(imageName) <* deleteImage(imageName))
        .flatMap {
          case (exitCode, testReport) =>
            reportParser
              .parse(testReport)
              .map(results => (exitCode, results))
        }
        .map {
          case (containerExit, testResults) =>
            logger.info(s"Evaluation done: ${containerExit.toString}")
            if (containerExit.statusCode() == 0)
              Success(containerExit.statusCode().toString, testResults)
            else
              Failure(containerExit.statusCode, testResults)

        }
  }

  private def deleteImage(imageName: DockerImageName): F[Unit] =
    F.delay {
      logger.info(s"Deleting image: ${imageName}")
      dockerClient.removeImage(imageName.asString)
      logger.info(s"Deleted image ${imageName}...")
    }

  private def progressHandler(imageName: String): ProgressHandler =
    (message: ProgressMessage) =>
      if (message != null && message
            .stream() != null && message.stream.replaceAll("\\s", "").nonEmpty)
        logger.info(s"${imageName.take(14)}...${imageName.takeRight(5)} progress: ${message
          .stream()
          .trim}")

  def runContainer(dockerImageName: DockerImageName): F[(ContainerExit, String)] =
    F.delay {
        val containerConfig: ContainerConfig = ContainerConfig
          .builder()
          .image(dockerImageName.asString)
          .workingDir("/project-solution")
          .build()
        val creation    = dockerClient.createContainer(containerConfig)
        val containerId = creation.id().asDockerContainerId
        dockerClient.startContainer(containerId.asString)

        import com.spotify.docker.client.DockerClient.AttachParameter

        val stream: LogStream = dockerClient.attachContainer(containerId.asString,
                                                             AttachParameter.LOGS,
                                                             AttachParameter.STDOUT,
                                                             AttachParameter.STDERR,
                                                             AttachParameter.STREAM)

        stream.attach(System.out, System.err, false)
        while (dockerClient.inspectContainer(containerId.asString).state.running()) {
          Thread.sleep(1000)
          val info = dockerClient.inspectContainer(containerId.asString)
          logger.info(info.toString)
        }

        logger.info(dockerClient.inspectContainer(containerId.asString).state.exitCode().toString)

        val reportAsString = getReport(containerId)
        dockerClient.stopContainer(containerId.asString, 10)
        val result = dockerClient.waitContainer(containerId.asString)
        logger.info(s"Removing container: ${containerId}")
        dockerClient.removeContainer(containerId.asString)
        (result, reportAsString)
      }
      .recover {
        case t: Throwable =>
          logger.error(t)(t.getMessage)
          (() => -1, "")
      }

  private def getReport(dockerContainerId: DockerContainerId): String = {

    val stream = dockerClient.archiveContainer(
      dockerContainerId.asString,
      "/project-solution/exercises-1/target/test-reports/ExerciseSpec.xml")

    val tarStream: TarArchiveInputStream = new TarArchiveInputStream(stream)

    var nextEntry   = tarStream.getNextEntry
    var fileContent = ""
    while (nextEntry != null) {
      fileContent = CharStreams.toString(new InputStreamReader(tarStream, StandardCharsets.UTF_8))
      nextEntry = tarStream.getNextEntry
    }
    tarStream.close()

    fileContent
  }

  def build(extractedDir: File): F[(DockerImageName, DockerImageId)] = F.delay {
    val uuid      = java.util.UUID.randomUUID()
    val imageName = s"evaluate-$uuid"
    val dockerImageId = dockerClient
      .build(extractedDir.path, imageName, dockerfileName, progressHandler(imageName))
      .asDockerImageId
    logger.info(s"Successfully built: ${dockerImageId} with name ${imageName}")
    (imageName.asDockerImageName, dockerImageId)
  }

}
