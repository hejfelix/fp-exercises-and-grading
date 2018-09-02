package grading.docker
import java.lang

import better.files.File
import cats.effect.Sync
import cats.implicits._
import com.spotify.docker.client.messages._
import com.spotify.docker.client.{DockerClient, LogStream, ProgressHandler}

sealed trait EvaluationResult
case class Success(s: String)                     extends EvaluationResult
case class Failure(exitCode: Long, error: String) extends EvaluationResult
class Evaluator[F[_]](dockerClient: DockerClient, dockerfileName: String)(implicit F: Sync[F]) {

  private val logger = org.log4s.getLogger

  def evaluate(extractedDir: File): F[EvaluationResult] = build(extractedDir) >>= {
    case (imageName, _) =>
      (runContainer(imageName) <* deleteImage(imageName))
        .map(containerExit => {
          logger.info(s"Evaluation done: ${containerExit.toString}")
          if (containerExit.statusCode() == 0)
            Success(containerExit.statusCode().toString)
          else
            Failure(containerExit.statusCode, "")
        })
  }

  private def deleteImage(imageName: DockerImageName): F[Unit] =
    F.delay {
      logger.info(s"Deleting image: ${imageName}")
      dockerClient.removeImage(imageName.asString)
      logger.info(s"Deleted image ${imageName}...")
    }

  private def progressHandler(imageName: String): ProgressHandler =
    (message: ProgressMessage) =>
      logger.info(s"Docker image ${imageName} progress: ${message.toString}")

  def runContainer(dockerImageName: DockerImageName): F[ContainerExit] =
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
        println("BLAAAAAAAAAAAAAAAAAAAAA")
        while (dockerClient.inspectContainer(containerId.asString).state.running()) {
          Thread.sleep(1000)
          val info = dockerClient.inspectContainer(containerId.asString)
          logger.info(info.toString)
        }

        logger.info(dockerClient.inspectContainer(containerId.asString).state.exitCode().toString)
        // Exec command inside running container with attached STDOUT and STDERR// Exec command inside running container with attached STDOUT and STDERR

//        val _                 = output.readFully

        dockerClient.stopContainer(containerId.asString, 10)
        val result = dockerClient.waitContainer(containerId.asString)
        logger.info(s"Removing container: ${containerId}")
        dockerClient.removeContainer(containerId.asString)
        result
      }
      .recover {
        case t: Throwable =>
          logger.error(t)(t.getMessage)
          new ContainerExit {
            override def statusCode(): lang.Long = -1
          }

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
