val zioVersion = "1.0.6"
val doobieVersion = "0.13.0"
val http4sVersion = "0.22.0-M7"
val circeVersion = "0.14.0-M5"
val jwtScalaVersion = "7.1.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "nomad-contest-zio",
    version := "0.1.0",

    scalaVersion := "3.0.0-RC2",
    scalacOptions ++= Seq(
      "-Ykind-projector"
    ),

    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"                 % zioVersion,
      "dev.zio"               %% "zio-streams"         % zioVersion,
      "dev.zio"               %% "zio-interop-cats"    % "2.4.1.0",
      "org.tpolecat"          %% "doobie-core"         % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"     % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % doobieVersion,
      "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
      "io.circe"              %% "circe-core"          % circeVersion,
      "io.circe"              %% "circe-generic"       % circeVersion,
      "com.github.jwt-scala"  %% "jwt-core"            % jwtScalaVersion,
      "com.github.pureconfig" %% "pureconfig-core"     % "0.15.1-SNAPSHOT",
      "org.flywaydb"          % "flyway-core"          % "7.8.1",
      "org.mindrot"           % "jbcrypt"              % "0.4",
      "ch.qos.logback"        % "logback-classic"      % "1.2.3",
      "com.novocode"          % "junit-interface"      % "0.11" % Test
    )
  )
