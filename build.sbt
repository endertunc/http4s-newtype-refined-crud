lazy val CatsEffectVersion          = "2.1.3"
lazy val Fs2Version                 = "2.3.0"
lazy val Http4sVersion              = "0.21.4"
lazy val CirceVersion               = "0.13.0"
lazy val DoobieVersion              = "0.9.0"
lazy val FlywayVersion              = "6.4.1"
lazy val LogbackVersion             = "1.2.3"
lazy val ScalaTestVersion           = "3.1.1"
lazy val ScalaCheckVersion          = "1.14.3"
lazy val PureConfigVersion          = "0.12.3"
lazy val TsecVersion                = "0.2.0-M2"
lazy val TestContainersScalaVersion = "0.36.1"
lazy val Log4CatsVersion            = "1.0.1"
lazy val BetterMonadicForVersion    = "0.3.1"
lazy val RandomDataGeneratorVersion = "2.8"
lazy val RefinedVersion             = "0.9.14"
lazy val PgEmbededVersion           = "0.13.3"
lazy val DerivingVersion            = "2.0.0-M5"
lazy val TapirVersion               = "0.14.3"
lazy val EndpointsVersion           = "0.15.0"
lazy val SortImportsVersion         = "0.3.2"
lazy val ScalaTracingVersion        = "2.2.0"
lazy val CatsVersion                = "0.25"
lazy val EnumeratumVersion          = "1.5.15"
lazy val EnumeratumDoobie           = "1.5.17"
lazy val EnumeratumCirce            = "1.5.23"
lazy val EnumeratumQiull            = "1.5.15"
lazy val EnumeratumScalacheck       = "1.5.16"
lazy val NewtypeVersion             = "0.4.3"
lazy val CatsScalaTestVersion       = "3.0.5"
lazy val KindProjectorVersion       = "0.11.0"
lazy val ScalaMacrosParadiseVersion = "2.1.1"

lazy val root = (project in file("."))
  .settings(scalastyleSettings ++ testSettings)
  .settings(
    organization := "com.ender",
    name := "car-advert",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.12.10",
    resolvers += Resolver.bintrayRepo("colisweb", "maven"), // scala-tracing
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions := Seq(
        "-unchecked",
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-Ypartial-unification",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-Yrangepos",
        "-Ywarn-unused-import"
      ),
    ThisBuild / scalafixDependencies ++= List(
        "com.nequissimus" %% "sort-imports" % SortImportsVersion
      ),
    libraryDependencies ++= Seq(
        "org.typelevel"            %% "cats-effect"                           % CatsEffectVersion,
        "co.fs2"                   %% "fs2-core"                              % Fs2Version,
        "com.github.pureconfig"    %% "pureconfig"                            % PureConfigVersion,
        "org.flywaydb"             % "flyway-core"                            % FlywayVersion,
        "io.chrisdavenport"        %% "log4cats-slf4j"                        % Log4CatsVersion,
        "org.http4s"               %% "http4s-blaze-server"                   % Http4sVersion,
        "org.http4s"               %% "http4s-circe"                          % Http4sVersion,
        "org.http4s"               %% "http4s-dsl"                            % Http4sVersion,
        "io.circe"                 %% "circe-core"                            % CirceVersion,
        "io.circe"                 %% "circe-generic"                         % CirceVersion,
        "io.circe"                 %% "circe-parser"                          % CirceVersion,
        "io.circe"                 %% "circe-literal"                         % CirceVersion,
        "io.circe"                 %% "circe-shapes"                          % CirceVersion,
        "io.circe"                 %% "circe-refined"                         % CirceVersion,
        "io.circe"                 %% "circe-generic-extras"                  % CirceVersion,
        "org.tpolecat"             %% "doobie-core"                           % DoobieVersion,
        "org.tpolecat"             %% "doobie-postgres"                       % DoobieVersion,
        "org.tpolecat"             %% "doobie-refined"                        % DoobieVersion,
        "org.tpolecat"             %% "doobie-hikari"                         % DoobieVersion,
        "org.tpolecat"             %% "doobie-quill"                          % DoobieVersion,
        "ch.qos.logback"           % "logback-classic"                        % LogbackVersion,
        "eu.timepit"               %% "refined"                               % RefinedVersion,
        "eu.timepit"               %% "refined-pureconfig"                    % RefinedVersion,
        "eu.timepit"               %% "refined-cats"                          % RefinedVersion,
        "org.typelevel"            %% "mouse"                                 % CatsVersion,
        "com.colisweb"             %% "scala-opentracing-http4s-server-tapir" % ScalaTracingVersion,
        "com.beachape"             %% "enumeratum"                            % EnumeratumVersion,
        "com.beachape"             %% "enumeratum-doobie"                     % EnumeratumDoobie,
        "com.beachape"             %% "enumeratum-circe"                      % EnumeratumCirce,
        "com.beachape"             %% "enumeratum-quill"                      % EnumeratumQiull,
        "io.estatico"              %% "newtype"                               % NewtypeVersion,
        "org.scalaz"               %% "deriving-macro"                        % DerivingVersion,
        "org.scalatest"            %% "scalatest"                             % ScalaTestVersion % Test,
        "org.scalacheck"           %% "scalacheck"                            % ScalaCheckVersion % Test,
        "org.tpolecat"             %% "doobie-scalatest"                      % DoobieVersion % Test,
        "com.dimafeng"             %% "testcontainers-scala-scalatest"        % TestContainersScalaVersion % Test,
        "com.dimafeng"             %% "testcontainers-scala-postgresql"       % TestContainersScalaVersion % Test,
        "eu.timepit"               %% "refined-scalacheck"                    % RefinedVersion % Test,
        "com.beachape"             %% "enumeratum-scalacheck"                 % EnumeratumScalacheck % Test,
        "com.ironcorelabs"         %% "cats-scalatest"                        % CatsScalaTestVersion % Test,
        "com.opentable.components" % "otj-pg-embedded"                        % PgEmbededVersion % Test,
        compilerPlugin("org.scalaz"      %% "deriving-plugin"    % DerivingVersion cross CrossVersion.full),
        compilerPlugin("org.typelevel"   %% "kind-projector"     % KindProjectorVersion cross CrossVersion.full),
        compilerPlugin("com.olegpy"      %% "better-monadic-for" % BetterMonadicForVersion),
        compilerPlugin("org.scalamacros" % "paradise"            % ScalaMacrosParadiseVersion cross CrossVersion.full)
      )
  )

// -----------------------------------------------------------------------------
// scalastyle settings
// -----------------------------------------------------------------------------

lazy val scalastyleSettings = Seq(
  scalastyleFailOnWarning := true
)

// -----------------------------------------------------------------------------
// scalastyle settings
// -----------------------------------------------------------------------------

lazy val testSettings = Seq(
  Test / parallelExecution := true,
  Test / concurrentRestrictions := Seq(
      Tags.limit(Tags.Test, max = 4) // scalastyle:ignore
    ),
  Test / logBuffered := false,
  Test / fork := true,
  Test / testForkedParallel := true,
  Test / testOptions += Tests.Argument("-oFDS")
)

// -----------------------------------------------------------------------------
// other settings
// -----------------------------------------------------------------------------

Global / onChangedBuildSource := ReloadOnSourceChanges

addCommandAlias("qa", "; clean; compile; coverage; test; coverageReport; coverageAggregate")
addCommandAlias("styleImports", "; rmu; si")
addCommandAlias("srmu", "scalafix RemoveUnused")
addCommandAlias("trmu", "test:scalafix RemoveUnused")
addCommandAlias("rmu", "; srmu; trmu")
addCommandAlias("ssi", "scalafix SortImports")
addCommandAlias("tsi", "test:scalafix SortImports")
addCommandAlias("si", "; ssi; tsi")
