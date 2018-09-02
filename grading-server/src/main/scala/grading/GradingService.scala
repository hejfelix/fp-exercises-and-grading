package grading
import better.files._
import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import fs2.io.file.writeAll
import fs2.{Sink, StreamApp}
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{DecodeResult, EntityDecoder, HttpService, Response}

import scala.concurrent.ExecutionContext

class GradingService[F[_]](implicit F: Effect[F], ec: ExecutionContext)
    extends StreamApp[F]
    with Http4sDsl[F] {

  private val logger = org.log4s.getLogger

  private def handlePart(filePart: Part[F]): F[File] = {
    val createTmpFile: F[File] = F.delay(File.newTemporaryFile(suffix = ".zip"))
    val tmpFolder              = F.delay(File.temporaryDirectory())
    val bodyStream             = filePart.body
    val createFileSink: F[(Sink[F, Byte], File)] =
      createTmpFile.map(file => (writeAll(file.path), file))
    val writeZipFile: F[File] = createFileSink.flatMap {
      case (sink, file) => bodyStream.to(sink).compile.drain *> F.pure(file)
    }
    writeZipFile
      .flatMap(file => tmpFolder.map(tmpFolder => (file, tmpFolder)))
      .map {
        case (file, tmpDir) =>
          println(tmpDir.get)
          file.unzipTo(tmpDir.get)
      }
  }

  private val helloWorldService = HttpService[F] {
    case request @ POST -> Root / "hello" =>
      val multipart: DecodeResult[F, Multipart[F]] =
        EntityDecoder[F, Multipart[F]].decode(request, strict = true)

      val r: EitherT[F, Exception, File] = multipart
        .flatMapF { multipart =>
          println(multipart.parts)
          val maybeFilePart: Option[Part[F]] =
            multipart.parts.find(_.filename == Option("exercise.zip"))
          maybeFilePart
            .traverse(handlePart)
            .map(_.toRight(new Exception("No exercise.zip part in request")))
        }
        .recoverWith {
          case e: Exception =>
            EitherT.leftT(e)
        }

      val response: F[Response[F]] = r
        .fold({ e =>
          logger.error(e)(e.getMessage)
          InternalServerError(e.getMessage)
        }, _ => Ok())
        .flatten
      response
  }

  override def stream(args: List[String],
                      requestShutdown: F[Unit]): fs2.Stream[F, StreamApp.ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService, "/")
      .serve
}
