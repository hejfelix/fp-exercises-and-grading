package grading
import io.estatico.newtype.macros.newtype
package object docker {
  @newtype case class DockerImageId(asString: String)
  @newtype case class DockerImageName(asString: String)
  @newtype case class DockerContainerId(asString: String)
  implicit class StringTaggable(s: String) {
    def asDockerImageId     = DockerImageId(s)
    def asDockerImageName   = DockerImageName(s)
    def asDockerContainerId = DockerContainerId(s)
  }
}
