val zioVersion = "1.0.9"
val zioLogging = "0.5.11"
val doobieVersion = "0.13.3"
val http4sVersion = "0.22.0-RC1"
val circeVersion = "0.14.0-M7"
val jwtScalaVersion = "7.1.5"
val pureconfigVersion = "0.16.0"
val testcontainersVersion = "0.39.5"

lazy val root = project
  .in(file("."))
  .settings(
    name := "nomad-contest-zio",
    version := "0.1.0",

    scalaVersion := "3.0.0",
    scalacOptions ++= Seq(
      "-Ykind-projector"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),

    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                 % zioVersion,
      "dev.zio"               %% "zio-streams"         % zioVersion,
      "dev.zio"               %% "zio-logging"         % zioLogging,
      "dev.zio"               %% "zio-logging-slf4j"   % zioLogging,
      "dev.zio"               %% "zio-interop-cats"    % "2.5.1.0",
      "org.tpolecat"          %% "doobie-core"         % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"     % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % doobieVersion,
      "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
      "io.circe"              %% "circe-core"          % circeVersion,
      "io.circe"              %% "circe-parser"        % circeVersion,
      "io.circe"              %% "circe-generic"       % circeVersion,
      "com.github.jwt-scala"  %% "jwt-core"            % jwtScalaVersion,
      "com.github.pureconfig" %% "pureconfig-core"     % pureconfigVersion,
      "org.flywaydb"          % "flyway-core"          % "7.8.1",
      "org.mindrot"           % "jbcrypt"              % "0.4",
      "ch.qos.logback"        % "logback-classic"      % "1.2.3"
    ),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface"                  % "0.11" % Test,
      "dev.zio"      %% "zio-test"                        % zioVersion % Test,
      "dev.zio"      %% "zio-test-sbt"                    % zioVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-scalatest"  % testcontainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersVersion % Test
    )
  )
