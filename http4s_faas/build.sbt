val Http4sVersion       = "0.18.18"
val Specs2Version       = "4.2.0"
val LogbackVersion      = "1.2.3"
val BetterFilesVersion  = "3.6.0"
val Log4sVersion        = "1.6.1"
val CommonsEmailVersion = "1.5"
lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "http4s_faas",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "org.http4s"           %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"           %% "http4s-circe"        % Http4sVersion,
      "org.http4s"           %% "http4s-dsl"          % Http4sVersion,
      "com.github.pathikrit" %% "better-files"        % BetterFilesVersion,
      "org.apache.commons"   % "commons-email"        % CommonsEmailVersion,
      "org.specs2"           %% "specs2-core"         % Specs2Version % "test",
      "org.log4s"            %% "log4s"               % Log4sVersion,
      "ch.qos.logback"       % "logback-classic"      % LogbackVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
  )
