package com.example
import scala.concurrent.duration.Duration

sealed trait TestResult {
  def name: String
}
case class Succeeded(duration: Duration, name: String)                    extends TestResult
case class Failed(errorMessage: String, duration: Duration, name: String) extends TestResult
