import sbt.{Def, _}
import sbt.Keys._

object Dependencies extends AutoPlugin {

  private val DockerTestkitVersion = "0.9.6"
  private val Fs2Version = "0.10.5"
  private val DoobieVersion = "0.5.1"
  private val CirceVersion = "0.10.0-M1"

  private val fs2 =
    Seq(
      "co.fs2" %% "fs2-core"
    ).map(_ % Fs2Version)

  private val circe =
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % CirceVersion)

  private val logging =
    Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2")

  private val doobie =
    Seq(
      "org.tpolecat" %% "doobie-hikari",
      "org.tpolecat" %% "doobie-postgres")
      .map(_ % DoobieVersion)

  private val database =
    Seq(
      "com.zaxxer" % "HikariCP" % "2.7.8",
      "org.postgresql" % "postgresql" % "42.2.2",
      "org.flywaydb" % "flyway-core" % "5.0.7")

  private val test =
    Seq(
      "org.scalatest" %% "scalatest" % "3.0.5",
      "com.ironcorelabs" %% "cats-scalatest" % "2.3.1",
      "eu.timepit" %% "refined-scalacheck" % "0.8.7",
      "org.scalamock" %% "scalamock" % "4.1.0",
      "org.tpolecat" %% "doobie-scalatest" % DoobieVersion,
      "com.whisk" %% "docker-testkit-impl-spotify" % DockerTestkitVersion,
      "com.whisk" %% "docker-testkit-scalatest" % DockerTestkitVersion,
      "com.spotify" % "docker-client" % "8.11.2")
      .map(_ % Test)

  private val defaultDependencies: Seq[Def.Setting[Seq[ModuleID]]] =
    Seq(
      libraryDependencies ++=
        fs2 ++
          circe ++
          database ++
          doobie ++
          logging ++
          test)

  object autoImport {

    implicit final class DependenciesProject(val project: Project) extends AnyVal {
      def withDependencies: Project =
        project.settings(defaultDependencies)
          .settings(Resolvers.default)
          .settings(Seq(
            addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
          ))
    }

  }

}
