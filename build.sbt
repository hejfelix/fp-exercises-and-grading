import sbt.Keys.{scalacOptions, version}

val v = Versions
lazy val exercises1 = project
  .in(file("exercises-1"))
  .settings(
    name := "Exercices 1",
    version := "0.1",
    scalaVersion := v.scala,
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-core"   % v.cats,
      "org.scalatest"  %% "scalatest"   % v.scalatest % Test,
      "org.scalacheck" %% "scalacheck"  % v.scalacheck % Test,
      "gov.nist.math"  % "jama"         % v.jama % Test,
      "org.slf4j"      % "slf4j-simple" % v.slf4j
    ),
    scalacOptions += "-Ypartial-unification"
  )

lazy val gradingServer = project
  .in(file("grading-server"))
  .settings(
    name := "Grading Server",
    version := "0.1",
    scalaVersion := v.scala,
    libraryDependencies ++= Seq(
      "org.http4s"           %% "http4s-blaze-server" % v.http4s,
      "org.http4s"           %% "http4s-dsl"          % v.http4s,
      "org.typelevel"        %% "cats-effect"         % v.catsEffect,
      "com.github.pathikrit" %% "better-files"        % v.betterfiles,
      "org.slf4j"            % "slf4j-simple"         % v.slf4j
    )
  )
