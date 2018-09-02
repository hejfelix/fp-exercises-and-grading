import sbt.Keys.{scalacOptions, version}
val v = Versions
lazy val exercises1 = project
  .in(file("exercises-1"))
  .settings(
    name := "Exercices 1",
    version := "0.1",
    scalaVersion := v.scala,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-core"  % v.cats,
      "org.scalatest"  %% "scalatest"  % v.scalatest % Test,
      "org.scalacheck" %% "scalacheck" % v.scalacheck % Test,
      "gov.nist.math"  % "jama"        % v.jama % Test,
    ),
    scalacOptions += "-Ypartial-unification"
  )

lazy val gradingServer = project.in(file("grading-server")).settings(
  name := "Grading Server",
  version := "0.1",
  scalaVersion := v.scala,
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-blaze-server" % "0.18.16"
  )
)