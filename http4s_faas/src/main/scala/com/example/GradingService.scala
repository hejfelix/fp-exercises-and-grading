package com.example

import better.files.File
import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import fs2.io.file.writeAll
import fs2.{Sink, async}
import io.circe.Json
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{DecodeResult, EntityDecoder, HttpService}

import scala.concurrent.ExecutionContext

class GradingService[F[_]](implicit F: Effect[F], ec: ExecutionContext) extends Http4sDsl[F] {

  private val logger    = org.log4s.getLogger
  private val evaulator = new Evaluator[F](new TestReportParser[F]())
  private val emailCredentials =
    EmailCredentials(???, ???)
  private val emailConfig: EmailConfig = EmailConfig(
    hostname = "smtp.zoho.com",
    port = 465,
    senderEmail = "no-reply@lambdaminute.com",
    senderName = "Grading Service",
    useSsl = true,
    credentials = Some(emailCredentials)
  )
  private val emailNotification = new EmailNotification[F](emailConfig)
  private val htmlTemplate      = new HtmlTemplate
  private val user              = User(???, ???)

  private def extractFilePartToDisk(filePart: Part[F]): F[File] = {
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
        case (file, tmpDir) => file.unzipTo(tmpDir.get)
      }
  }

  private def handleResult(maybeResult: Either[Throwable, EvaluationResult]): F[Unit] =
    maybeResult.fold(
      err => F.delay(logger.error(err)(err.getMessage)),
      res => {
        F.delay {
          logger.info(s"HANDLE RESULT: ${res.toString}")
        } *>
          emailNotification
            .notifyUserHtml(user, "Test results")(htmlTemplate.render(res))
            .map(_ => ())
      }
    )

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / path =>
        Ok(Json.obj("message" -> Json.fromString(s"Hello, ${path}")))
      case req @ POST -> Root =>
        val multiparDecoder = EntityDecoder[F, Multipart[F]].decode(req, strict = true)
        val multipart: DecodeResult[F, Multipart[F]] =
          EntityDecoder[F, Multipart[F]].decode(req, strict = true)

        val unzipOp: EitherT[F, Exception, File] = multipart
          .flatMapF { multipart =>
            val maybeFilePart: Option[Part[F]] =
              multipart.parts.find(_.filename == Option("exercise.zip"))
            maybeFilePart
              .traverse(extractFilePartToDisk)
              .map(_.toRight(new Exception("No exercise.zip part in request")))
          }
          .recoverWith {
            case e: Exception =>
              EitherT.leftT(e)
          }

        val withEvaluationAsync: EitherT[F, Exception, File] =
          unzipOp.semiflatMap(
            unzippedDirectory =>
              async
                .start(evaulator.evaluate(unzippedDirectory).attempt.flatMap(handleResult))
                .map(_ => unzippedDirectory))

        withEvaluationAsync
          .fold({ e =>
            logger.error(e)(e.getMessage)
            InternalServerError(e.getMessage)
          }, _ => Ok())
          .flatten
    }
  }
}
