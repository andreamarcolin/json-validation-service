import sbt._
import sbt.librarymanagement.ModuleID

object Build {
  object Versions {
    val zio        = "1.0.0-RC17"
    val cats       = "2.0.0"
    val catsEffect = "2.0.0"
    val zioMacros  = "0.6.2"
    val doobie     = "0.8.8"
    val circe      = "0.13.0"
    val pureConfig = "0.12.2"
    val http4s     = "0.21.1"
  }

  val dependencies: Seq[ModuleID] = Seq(
    "dev.zio"                    %% "zio"                  % Versions.zio,
    "dev.zio"                    %% "zio-interop-cats"     % (Versions.catsEffect + ".0-RC10"),
    "dev.zio"                    %% "zio-macros-core"      % Versions.zioMacros,
    "dev.zio"                    %% "zio-logging-slf4j"    % "0.2.1",
    "org.tpolecat"               %% "doobie-core"          % Versions.doobie,
    "org.tpolecat"               %% "doobie-postgres"      % Versions.doobie,
    "org.tpolecat"               %% "doobie-hikari"        % Versions.doobie,
    "org.http4s"                 %% "http4s-dsl"           % Versions.http4s,
    "org.http4s"                 %% "http4s-blaze-client"  % Versions.http4s,
    "org.http4s"                 %% "http4s-blaze-server"  % Versions.http4s,
    "org.http4s"                 %% "http4s-circe"         % Versions.http4s,
    "io.circe"                   %% "circe-core"           % Versions.circe,
    "io.circe"                   %% "circe-literal"        % Versions.circe,
    "io.circe"                   %% "circe-generic"        % Versions.circe,
    "io.circe"                   %% "circe-generic-extras" % Versions.circe,
    "io.circe"                   %% "circe-parser"         % Versions.circe,
    "io.circe"                   %% "circe-json-schema"    % "0.1.0",
    "org.typelevel"              %% "cats-core"            % Versions.cats,
    "org.typelevel"              %% "cats-effect"          % Versions.catsEffect,
    "com.github.pureconfig"      %% "pureconfig"           % Versions.pureConfig,
    "com.lihaoyi"                %% "sourcecode"           % "0.1.7",
    "org.postgresql"             % "postgresql"            % "42.2.8",
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.12",
    "dev.zio"                    %% "zio-test"             % Versions.zio % "test",
    "dev.zio"                    %% "zio-test-sbt"         % Versions.zio % "test",
    compilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full),
    compilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )

  val welcomeMessage: String = {
    import scala.Console._

    def item(text: String): String = s"${GREEN}▶ ${CYAN}$text${RESET}"

    s"""|Useful sbt tasks:
        |${item("check")}         - Check source files formatting using scalafmt
        |${item("fmt")}           - Formats source files using scalafmt
        |${item("clean")}         - Clean target directory
        |${item("test")}          - Run tests
        |${item("packageJar")}    - Package the app as a fat JAR
      """.stripMargin
  }
}
