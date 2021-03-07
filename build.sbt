ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.0.1"
ThisBuild / organization := "com.github.tzarouali"

val Http4sVersion         = "0.21.20"
val CatsVersion           = "2.4.1"
val CatsEffectVersion     = "2.3.3"
val Fs2Version            = "2.5.3"
val ScalaNewtypeVersion   = "0.4.4"
val CirceVersion          = "0.13.0"
val CirceConfigVersion    = "0.8.0"
val LogbackVersion        = "1.2.3"
val LogbackEncoderVersion = "6.6"
val Fs2RabbitVersion      = "3.0.1"
val JooqVersion           = "3.14.8"
val PostgresVersion       = "42.2.19"
val HikariVersion         = "4.0.3"
val ScalaTestVersion      = "3.2.5"
val CatsScalatestVersion  = "3.1.1"
val H2DatabaseVersion     = "1.4.200"
val ScalamockVersion      = "5.1.0"
val kindProjectorVersion  = "0.11.3"
val BetterMonadicVersion  = "0.3.1"
val Fs2CronVersion        = "0.2.2"
val Cron4CirceVersion     = "0.6.1"

val genJooqModel = taskKey[Unit]("Generate JOOQ classes")
val genJooqTask = Def.task {
  val classpath   = (dependencyClasspath in Compile).value
  val scalaRun    = (runner in Compile).value
  val taskStreams = streams.value
  scalaRun
    .run("org.jooq.codegen.GenerationTool", classpath.files, Seq("src/main/resources/jooq.xml"), taskStreams.log)
    .failed
    .foreach(e => sys.error(e.getMessage))
}

lazy val root = (project in file("."))
  .settings(
    name := "crypto_scrapper",
    scalacOptions.in(Compile) ~= (opts => opts.filterNot(_ == "-Xlint:package-object-classes")),
    scalacOptions.in(Compile) ++= Seq("-Ymacro-annotations"),
    genJooqModel := genJooqTask.value,
    libraryDependencies ++= Seq(
      //
      // Http requests
      //
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      //
      // :=)
      //
      "org.typelevel" %% "cats-core"   % CatsVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "co.fs2"        %% "fs2-core"    % Fs2Version,
      //
      // Newtypes
      //
      "io.estatico" %% "newtype" % ScalaNewtypeVersion,
      //
      // JSON
      //
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion,
      "io.circe" %% "circe-config"  % CirceConfigVersion,
      //
      // DB
      //
      "org.jooq"       % "jooq"         % JooqVersion,
      "org.jooq"       % "jooq-meta"    % JooqVersion,
      "org.jooq"       % "jooq-codegen" % JooqVersion,
      "org.jooq"       %% "jooq-scala"  % JooqVersion,
      "org.postgresql" % "postgresql"   % PostgresVersion,
      "com.zaxxer"     % "HikariCP"     % HikariVersion,
      //
      // Cron expressions
      //
      "eu.timepit"                    %% "fs2-cron-core" % Fs2CronVersion,
      "com.github.alonsodomin.cron4s" %% "cron4s-circe"  % Cron4CirceVersion,
      //
      // Rabbit
      //
      "dev.profunktor" %% "fs2-rabbit"       % Fs2RabbitVersion,
      "dev.profunktor" %% "fs2-rabbit-circe" % Fs2RabbitVersion,
      //
      // Logging
      //
      "ch.qos.logback"       % "logback-classic"          % LogbackVersion,
      "net.logstash.logback" % "logstash-logback-encoder" % LogbackEncoderVersion,
      //
      // Tests
      //
      "org.scalatest"    %% "scalatest"      % ScalaTestVersion     % Test,
      "org.scalamock"    %% "scalamock"      % ScalamockVersion     % Test,
      "com.ironcorelabs" %% "cats-scalatest" % CatsScalatestVersion % Test,
      "com.h2database"   % "h2"              % H2DatabaseVersion    % Test
    ),
    addCompilerPlugin(("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicVersion),
    parallelExecution in Test := false
  )
