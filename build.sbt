import sbt.Keys.{scalacOptions, version}

val v = Versions
lazy val exercises1 = project
  .in(file("exercises-1"))
  .settings(
    name := "Exercices 1",
    version := "0.1",
    scalaVersion := v.scala,
    libraryDependencies ++= Seq(
      "gov.nist.math"  % "jama"         % v.jama % Test,
      "org.scalacheck" %% "scalacheck"  % v.scalacheck % Test,
      "org.scalatest"  %% "scalatest"   % v.scalatest % Test,
      "org.slf4j"      % "slf4j-simple" % v.slf4j,
      "org.typelevel"  %% "cats-core"   % v.cats,
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
      "ch.qos.logback"       % "logback-classic"      % v.logback,
      "ch.qos.logback"       % "logback-core"         % v.logback,
      "com.github.pathikrit" %% "better-files"        % v.betterfiles,
      "com.icegreen"         % "greenmail"            % v.greenmail,
      "com.spotify"          % "docker-client"        % v.spotifyDocker,
      "io.estatico"          %% "newtype"             % v.newtype,
      "org.http4s"           %% "http4s-blaze-server" % v.http4s,
      "org.http4s"           %% "http4s-dsl"          % v.http4s,
      "org.log4s"            %% "log4s"               % v.log4s,
      "org.typelevel"        %% "cats-effect"         % v.catsEffect,
      "org.apache.commons"   % "commons-email"        % v.commonsEmail,
    ),
    scalacOptions -= "-Xlint:package-object-classes",
    addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full)
  )
