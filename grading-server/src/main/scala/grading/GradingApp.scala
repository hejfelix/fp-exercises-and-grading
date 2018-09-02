package grading

import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global

object GradingApp extends GradingService[IO]
