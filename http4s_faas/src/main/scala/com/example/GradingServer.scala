package com.example

import cats.effect.{Effect, IO}
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object HelloWorldServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger = org.log4s.getLogger
  Runtime.getRuntime().addShutdownHook(new Thread(){
    override def run(): Unit = logger.info("<=========== YAAY, WE GOT A SHUTDOWN HOOK!!! SUCK IT AWS LAMBDA :-p =========>")
  } )

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  def helloWorldService[F[_]: Effect](implicit ec: ExecutionContext) =
    new GradingService[F].service

  def stream[F[_]: Effect](implicit ec: ExecutionContext) =
    BlazeBuilder[F]
      .bindHttp(5000, "0.0.0.0")
      .mountService(helloWorldService, "/")
      .serve
}
